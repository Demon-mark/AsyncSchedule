package top.amosen.asyncSchedule.callback;

import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.scheduler.Scheduler;
import top.amosen.asyncSchedule.wrapper.AWorkerWrapper;

import java.util.Map;

/**
 * @author Amosen
 * @Date 2023-03-18 14:23
 */
public class SchedulerCallbackAdapter<P, R> implements ACallback<P, R> {

    private Scheduler scheduler;

    private AWorkerWrapper<P, R> wrapper;

    private ACallback<P, R> callback;

    public SchedulerCallbackAdapter(AWorkerWrapper<P, R> wrapper, ACallback<P, R> callback, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.wrapper = wrapper;
        if (null == callback) {
            callback = new DefaultCallbackAdapter<>();
        }
        this.callback = callback;
    }

    @Override
    public void onBegin(P param) {
        callback.onBegin(param);
    }

    @Override
    public boolean onError(Throwable throwable, R result) {
        boolean go = callback.onError(throwable, result);
        // 任务发生异常，通知调度器进行处理
        scheduler.errorWrapper(wrapper, throwable, result, go);
        return go;
    }

    @Override
    public void onResult(R result) {
        // 任务正常结束
        callback.onResult(result);
        scheduler.normalWrapper(wrapper, result);
    }

    @Override
    public void onComplete(P param, R result, Throwable throwable) {
        callback.onComplete(param, result, throwable);
        scheduler.endWrapper(wrapper, result, throwable);
    }

    @Override
    public void onFail(Throwable throwable, Map<String, AWorkerResult> results) {
        callback.onFail(throwable, results);
        scheduler.handleFail(wrapper, throwable);
    }


}
