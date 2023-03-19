package top.amosen.asyncSchedule.wrapper.impl;

import top.amosen.asyncSchedule.callback.ACallback;
import top.amosen.asyncSchedule.callback.DefaultCallbackAdapter;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;
import top.amosen.asyncSchedule.wrapper.AWorkerWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author Amosen
 * @Date 2023-03-16 10:56
 */
public class DefaultWorkerWrapper<P, R> implements AWorkerWrapper<P, R> {
    private ACallback<P, R> callback;

    private AWorker<P, R> worker;

    private P param;

    private String name;

    private Map<String, AWorkerResult> results = new ConcurrentHashMap<>();

    private static final String DEFAULT_NAME_PREFIX = "wrapper-";

    private static Integer wrapperNum = 0;

    private AtomicInteger quoted = new AtomicInteger(0);

    private AtomicBoolean failed = new AtomicBoolean(false);

    private AtomicBoolean hasSuccess = new AtomicBoolean(false);

    private Throwable failCause = null;

    public DefaultWorkerWrapper() {
        this(DEFAULT_NAME_PREFIX + (++wrapperNum));
    }

    public DefaultWorkerWrapper(String name) {
        this(name, null, new DefaultCallbackAdapter<>());
    }

    public DefaultWorkerWrapper(String name, AWorker<P, R> worker) {
        this(name, worker, new DefaultCallbackAdapter<>());
    }

    public DefaultWorkerWrapper(String name, AWorker<P, R> worker, ACallback<P, R> callback) {
        if (null == name) {
            name = DEFAULT_NAME_PREFIX + (++wrapperNum);
        }
        this.name = name;
        this.worker = worker;
        if (null == callback) {
            callback = new DefaultCallbackAdapter<>();
        }
        this.callback = callback;
    }

    @Override
    public void putResult(String param, AWorkerResult result) {
        results.put(param, result);
        hasSuccess.compareAndSet(false, true);
        quoted.getAndDecrement();
    }

    @Override
    public Integer getQuoted() {
        return quoted.get();
    }

    @Override
    public void addQuoted() {
        quoted.getAndIncrement();
    }

    @Override
    public void mustFail(Throwable throwable) {
        this.quoted.decrementAndGet();
        this.failed.compareAndSet(false, true);
        this.failCause = throwable;
    }

    @Override
    public void needFail(Throwable throwable) {
        this.quoted.decrementAndGet();
        if (this.quoted.get() > 0) {
            // 还有未完成的依赖，不能判定该任务失败
            return;
        }
        this.failed.compareAndSet(false, !this.hasSuccess.get());
        this.failCause = throwable;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    // getter for name
    @Override
    public String getName() {
        return name;
    }

    @Override
    public AWorkerWrapper<P, R> callback(ACallback<P, R> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public ACallback<P, R> callback() {
        return callback;
    }

    @Override
    public AWorkerWrapper<P, R> setWorker(AWorker<P, R> worker) {
        this.worker = worker;
        return this;
    }

    @Override
    public synchronized Runnable execute() {
        validate();
        if (failed.get()) {
            callback.onFail(failCause, results);
            return null;
        }
        return () -> {

            Throwable t = null;
            callback.onBegin(param);
            R result = null;
            try {
                result = worker.run(param, results);
                callback.onResult(result);
            } catch (Throwable throwable) {
                t = throwable;
                if (!callback.onError(t, result)) {
                    throw throwable;
                }
            } finally {
                callback.onComplete(param, result, t);
            }
        };
    }

    @Override
    public AWorkerWrapper<P, R> setParam(P param) {
        this.param = param;
        return this;
    }

    private void validate() {
        if (null == callback) {
            callback = new DefaultCallbackAdapter<>();
        }
    }
}
