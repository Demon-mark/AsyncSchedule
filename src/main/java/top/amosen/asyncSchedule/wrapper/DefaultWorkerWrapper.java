package top.amosen.asyncSchedule.wrapper;

import top.amosen.asyncSchedule.callback.ACallback;
import top.amosen.asyncSchedule.callback.DefaultCallbackAdapter;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;

import java.util.Map;
import java.util.concurrent.FutureTask;

/**
 * @author Amosen
 * @Date 2023-03-16 10:56
 */
public class DefaultWorkerWrapper<P, R> implements AWorkerWrapper<P, R> {

    private ACallback<P, R> callback;

    private AWorker<P, R> worker;

    private P param;

    @Override
    public AWorkerWrapper<P, R> callback(ACallback<P, R> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public ACallback<P, R> getCallback() {
        return callback;
    }

    @Override
    public AWorkerWrapper<P, R> setWorker(AWorker<P, R> worker) {
        this.worker = worker;
        return this;
    }

    @Override
    public FutureTask<AWorkerResult> execute(Map<String, AWorkerResult> results) {
        validate();
        return new FutureTask<>(() -> {
            AWorkerResult workerResult = new AWorkerResult();
            callback.onBegin(param);
            R result = null;
            try {
                result = worker.run(param, results);
                callback.onResult(result);
                workerResult.setResult(result);
            } catch (Throwable throwable) {
                boolean go = callback.onError(throwable, result);
                if (go) {
                    workerResult.setError(throwable);
                    workerResult.setResult(result);
                } else {
                    throw throwable;
                }
            }
            return workerResult;
        });
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
