package top.amosen.asyncSchedule.wrapper;

import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.scheduler.Scheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

/**
 * 可被调度的节点，单线程访问
 *
 * @author Amosen
 * @Date 2023-03-16 13:11
 */
public class AScheduleNode<P, R> {

    private AWorkerWrapper<P, R> wrapper;

    private FutureTask<AWorkerResult> task;

    private String name;

    // name -> node
    private Set<AScheduleNode> nextNodes = new HashSet<>();

    private volatile Integer quoted = 0;

    private Map<String, AWorkerResult> params = new ConcurrentHashMap<>();

    private Scheduler scheduler;

    public AScheduleNode(Scheduler scheduler) {
        this.scheduler = scheduler;
        register();
    }

    private void register() {
        scheduler.addExecutable(this);
    }

    public void setWrapper(AWorkerWrapper<P, R> wrapper) {
        this.wrapper = wrapper;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setQuoted(Integer quoted) {
        this.quoted = quoted;
        if (quoted >= 1) {
            scheduler.addWaiting(this);
        }
    }

    public Integer getQuoted() {
        return quoted;
    }

    public void setNextNode(String name, AScheduleNode node) {
        this.nextNodes.add(node);
        Integer nextQuoted = node.getQuoted();
        nextQuoted++;
        node.setQuoted(nextQuoted);
    }

    public void addParam(String name, AWorkerResult result) {
        this.params.put(name, result);
        --this.quoted;
        if (quoted == 0) {
            scheduler.removeWaiting(this);
        }
    }

    public AWorkerResult result() throws ExecutionException, InterruptedException {
        return this.task.get();
    }

    public void call(ExecutorService executorService) throws ExecutionException, InterruptedException {
        // 自旋锁等待，实际上，这一段不应该被多次循环，否则应当重新设计调度
        while (quoted != 0) {
        }
        FutureTask<AWorkerResult> task = wrapper.execute(params);
//        executorService.execute(task);
        executorService.execute(() -> {
            task.run();
            try {
                AWorkerResult result = task.get();
                nextNodes.forEach(node -> node.addParam(name, result));
                scheduler.addExecuted(this);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        this.task = task;
    }

}
