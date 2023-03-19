package top.amosen.asyncSchedule.scheduler;

import top.amosen.asyncSchedule.async.Async;
import top.amosen.asyncSchedule.callback.ACallback;
import top.amosen.asyncSchedule.callback.SchedulerCallbackAdapter;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;
import top.amosen.asyncSchedule.wrapper.AWorkerWrapper;
import top.amosen.asyncSchedule.wrapper.impl.DefaultWorkerWrapper;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * 调度，单线程处理
 * @author Amosen
 * @Date 2023-03-16 14:11
 */
public class Scheduler implements Iterator {

    private Logger logger = Logger.getLogger("top.amosen.asyncSchedule.scheduler.Scheduler");
    private final BlockingQueue<AWorkerWrapper> executable = new LinkedBlockingQueue<>();

    private final Set<AWorkerWrapper> whiteSet = new HashSet<>();

    private final Set<AWorkerWrapper> waiting = Collections.synchronizedSet(new HashSet<>());

    private final Map<String, AWorkerResult> executed = new ConcurrentHashMap<>();

    private final Set<AWorkerWrapper> executing = Collections.synchronizedSet(new HashSet<>());

    private final Map<String, AWorkerWrapper> registered = new HashMap<>();

    // key：A wrapper value： 强依赖A wrapper的所有wrapper
    private final Map<AWorkerWrapper, Set<AWorkerWrapper>> must = new ConcurrentHashMap<>();

    // key：A wrapper value： 弱依赖A wrapper的所有wrapper
    private final Map<AWorkerWrapper, Set<AWorkerWrapper>> need = new ConcurrentHashMap<>();

    private volatile boolean hasWrapper = false;

    private String name;

    private static final String DEFAULT_NAME_PREFIX = "scheduler-";

    /**
     * 记录全局已经创建的Scheduler数量，用于省略指定scheduler名称
     */
    private static Integer taskNum = 0;

    private CountDownLatch latch;

    public Scheduler() {
        this(DEFAULT_NAME_PREFIX + (++taskNum));
    }

    public Scheduler(String name) {
        this.name = name;
    }

    public boolean hasNext() {
        if (executable.isEmpty() && !waiting.isEmpty() && executing.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder("[ ");
            waiting.forEach(wrapper -> stringBuilder.append(wrapper.getName() + ", "));
            stringBuilder.append("]");
            throw new RuntimeException("circular transfer detected between worker: " + stringBuilder.toString() +
                    ", or a task's condition can not satisfy");
        }
        return !executable.isEmpty() || !waiting.isEmpty();
    }

    // executable -> executing
    public AWorkerWrapper next() {
        try {
            AWorkerWrapper wrapper = executable.take();
            executing.add(wrapper);
            return wrapper;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // when init whiteSet -> waiting
    private void addWaiting(AWorkerWrapper wrapper) {
        whiteSet.remove(wrapper);
        waiting.add(wrapper);
    }

    // when executing waiting -> executable
    private void removeWaiting(AWorkerWrapper wrapper) {
        boolean b = waiting.remove(wrapper);
        if (b) {
            executable.offer(wrapper);
        }
    }

    public <P, R> AWorkerWrapperFacade<P, R> newWorker(AWorker<P, R> worker) {
        return this.newWorker(null, worker);
    }

    public <P, R> AWorkerWrapperFacade<P, R> newWorker(String name, AWorker<P, R> worker) {
        AWorkerWrapper<P, R> wrapper = new DefaultWorkerWrapper<>(name, worker);
        hasWrapper = true;
        return new AWorkerWrapperFacade<>(wrapper, this);
    }

    public Scheduler must(String from, String to) {
        AWorkerWrapper source = registered.get(from);
        AWorkerWrapper dest = registered.get(to);
        if (source == dest) {
            throw new RuntimeException("do not transfer result to worker itself");
        }
        Set<AWorkerWrapper> dependents = must.getOrDefault(source, Collections.synchronizedSet(new HashSet<>()));
        dest.addQuoted();
        dependents.add(dest);
        must.put(registered.get(from), dependents);
        addWaiting(dest);
        return this;
    }

    public Scheduler must(String from, String...to) {
        for (String s : to) {
            must(from, s);
        }
        return this;
    }

    public Scheduler need(String from, String to) {
        AWorkerWrapper source = registered.get(from);
        AWorkerWrapper dest = registered.get(to);
        if (source == dest) {
            throw new RuntimeException("do not transfer result to worker itself");
        }
        Set<AWorkerWrapper> dependents = need.getOrDefault(source, Collections.synchronizedSet(new HashSet<>()));
        dest.addQuoted();
        dependents.add(dest);
        need.put(registered.get(from), dependents);
        addWaiting(dest);
        return this;
    }

    public Scheduler need(String from, String...to) {
        for (String s : to) {
            need(from, s);
        }
        return this;
    }

    void register(AWorkerWrapper wrapper) {
        // 初始化wrapper回调扩展
        init(wrapper);
        register0(wrapper);
    }

    private void register0(AWorkerWrapper wrapper) {
        registered.put(wrapper.getName(), wrapper);
        whiteSet.add(wrapper);
    }

    public void run(ExecutorService executorService) {
        validate();
        for (AWorkerWrapper wrapper : whiteSet) {
            executable.offer(wrapper);
        }
        Async.execute(executorService, this);
    }

    public void run() {
        run(null);
    }

    private void validate() {
        // 预检查是否有循环依赖的任务
        if (whiteSet.isEmpty() && (!must.isEmpty() || !need.isEmpty())) {
            throw new RuntimeException("circular transfer detected or a task's condition can not satisfy, please check");
        }
        // 初始化CountdownLatch
        latch = new CountDownLatch(registered.size());
    }

    private void init(AWorkerWrapper wrapper) {
        // 更新wrapper callback，适配scheduler调度能力
        wrapper.callback(new SchedulerCallbackAdapter(wrapper, wrapper.callback(), this));
    }

    private void addExecuted(String name, AWorkerResult result) {
        executed.put(name, result);
        hasWrapper = executed.size() < registered.size();
    }

    /**
     * 获取最终所有异步任务的执行结果
     * @return
     */
    public Map<String, AWorkerResult> results() throws InterruptedException {
        forAll();
        return executed;
    }

    public Map<String, AWorkerResult> results(long timeout, TimeUnit unit) throws InterruptedException {
        forAll(timeout, unit);
        return executed;
    }

    public <P, R> void errorWrapper(AWorkerWrapper<P, R> wrapper, Throwable throwable,
                                    R result, boolean go) {
        // 任务异常时，根据go来决定是继续执行还是快速失败
        if (!go) {
            // 每次快速失败都会导致递归的调用，大量的失败任务会导致整个应用性能的急剧下降
            fastFail(wrapper, throwable);
        }
        // 不论是否快速失败，都将结果放入wrapper中，便于后续结果查询
        continueGo(wrapper, throwable, result);
    }

    private void fastFail(AWorkerWrapper wrapper, Throwable throwable) {
        Set<AWorkerWrapper> mustDependents = must.getOrDefault(wrapper, null);
        if (null != mustDependents) {
            mustDependents.forEach(dependent -> {
                logger.warning("must fast fail " + dependent.getName() + " cause task " + wrapper.getName() + "failed");
                dependent.mustFail(throwable);
                fastFail(dependent, throwable);
            });
        }
        Set<AWorkerWrapper> needDependents = need.getOrDefault(wrapper, null);
        if (null != needDependents) {
            needDependents.forEach(dependent -> {
                logger.warning("weak fast fail " + dependent.getName() + " cause task " + wrapper.getName() + "failed");
                dependent.needFail(throwable);
                fastFail(dependent, throwable);
            });
        }
    }

    public <P, R> void normalWrapper(AWorkerWrapper<P, R> wrapper, R result) {
        continueGo(wrapper, null, result);
    }

    // executing -> executed
    public <P, R> void endWrapper(AWorkerWrapper<P, R> wrapper, R result, Throwable throwable) {
        // 任务结束，将结果放入
        AWorkerResult workerResult = packForResult(throwable, result);
        executing.remove(wrapper);
        addExecuted(wrapper.getName(), workerResult);
        latch.countDown();
    }

    private <P, R> void continueGo(AWorkerWrapper<P, R> wrapper, Throwable throwable, R result) {
        // 在这里不考虑快速失败的任务跳过，原因：
        // 被快速失败的任务并不影响wrapper中的被依赖数，后续的任务只能有一种情况：
        //      任务在等待集合中
        // 对于这种情况，最终在被调度器调度出来交给执行器执行时，会返回null，从而跳过执行器执行
        // 由于每个调度器都是单线程执行的，因此其并不会带来线程上下文切换的开销
        // 机制上，保证快速失败时，被依赖项一定处于等待集合中，而不可能处于正在执行的过程中，这会导致任务编排的失败
        Set<AWorkerWrapper> mustDependents = must.getOrDefault(wrapper, null);
        if (null != mustDependents) {
            mustDependents.forEach(dependent -> addResult(dependent, wrapper.getName(), packForResult(throwable, result)));
        }
        Set<AWorkerWrapper> needDependents = need.getOrDefault(wrapper, null);
        if (null != needDependents) {
            needDependents.forEach(dependent -> addResult(dependent, wrapper.getName(), packForResult(throwable, result)));
        }
    }

    private <R> AWorkerResult packForResult(Throwable throwable, R result) {
        AWorkerResult workerResult = new AWorkerResult();
        workerResult.setError(throwable);
        workerResult.setResult(result);
        return workerResult;
    }

    private void addResult(AWorkerWrapper wrapper, String name, AWorkerResult result) {
        wrapper.putResult(name, result);
        // 如果该dependent所有的依赖项全部执行完毕，则将其加入可执行队列
        if (wrapper.getQuoted() == 0) {
            removeWaiting(wrapper);
        }
    }

    public <P, R> void handleFail(AWorkerWrapper<P, R> wrapper, Throwable throwable) {
        waiting.remove(wrapper);
        addExecuted(wrapper.getName(), packForResult(throwable, null));
    }

    public void forAll() throws InterruptedException {
        latch.await();
    }

    public void forAll(long timeout, TimeUnit unit) throws InterruptedException {
        boolean finish = latch.await(timeout, unit);
        if (!finish) {
            // 超时，该组任务应当全部失败
            logger.warning("scheduler " + name + " execute fail for timeout");
            fastFailAll();
        }
    }

    public void fastFailAll() {
        // 先将waiting中的任务快速失败
        for (AWorkerWrapper wrapper : waiting) {
            fastFail(wrapper, new RuntimeException("fast fail for task timeout"));
        }
        // 快速失败executable中还未执行的任务
        synchronized (executable) {
            while (hasNext()) {
                fastFail(next(), new RuntimeException("fast fail for task timeout"));
            }
        }
    }


    public static class AWorkerWrapperFacade<P, R> {
        AWorkerWrapper<P, R> wrapper;

        private Scheduler scheduler;

        public AWorkerWrapperFacade(AWorkerWrapper<P, R> wrapper, Scheduler scheduler) {
            this.wrapper = wrapper;
            this.scheduler = scheduler;
        }

        public AWorkerWrapperFacade<P, R> callback(ACallback<P, R> callback) {
            this.wrapper.callback(callback);
            return this;
        }

        public AWorkerWrapperFacade<P, R> name(String name) {
            this.wrapper.setName(name);
            return this;
        }

        public AWorkerWrapperFacade<P, R> worker(AWorker<P, R> worker) {
            this.wrapper.setWorker(worker);
            return this;
        }

        public AWorkerWrapperFacade<P, R> param(P param) {
            this.wrapper.setParam(param);
            return this;
        }

        public void build() {
            scheduler.register(wrapper);
        }

    }

}
