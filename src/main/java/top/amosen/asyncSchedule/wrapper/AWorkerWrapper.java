package top.amosen.asyncSchedule.wrapper;

import top.amosen.asyncSchedule.callback.ACallback;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;

/**
 * @author Amosen
 * @Date 2023-03-15 17:22
 */
public interface AWorkerWrapper<P, R> {

    void setName(String name);

    String getName();

    /**
     * 设置执行回调
     * @param callback 执行回调
     */
    AWorkerWrapper<P, R> callback(ACallback<P, R> callback);


    ACallback<P, R> callback();

    /**
     * 设置worker
     * @param worker worker
     */
    AWorkerWrapper<P, R> setWorker(AWorker<P, R> worker);

    /**
     * 执行worker
     * @return 需要执行的任务
     */
    Runnable execute();

    /**
     * 设置参数，在worker调用时传递给对应的方法
     * @param param 需要传递的参数
     */
    AWorkerWrapper<P, R> setParam(P param);

    void putResult(String name, AWorkerResult result);

    Integer getQuoted();

    void addQuoted();

    void mustFail(Throwable throwable);

    void needFail(Throwable throwable);

}
