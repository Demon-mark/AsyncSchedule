package top.amosen.asyncSchedule.wrapper;

import com.sun.istack.internal.Nullable;
import top.amosen.asyncSchedule.callback.ACallback;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;

import java.util.Map;
import java.util.concurrent.FutureTask;

/**
 * @author Amosen
 * @Date 2023-03-15 17:22
 */
public interface AWorkerWrapper<P, R> {

    /**
     * 设置执行回调
     * @param callback 执行回调
     */
    AWorkerWrapper<P, R> callback(ACallback<P, R> callback);

    @Nullable
    ACallback<P, R> getCallback();

    /**
     * 设置worker
     * @param worker worker
     */
    AWorkerWrapper<P, R> setWorker(AWorker<P, R> worker);

    /**
     * 执行worker
     * @param results 所依赖的worker传递的结果
     * @return
     */
    FutureTask<AWorkerResult> execute(Map<String, AWorkerResult> results);

    /**
     * 设置参数，在worker调用时传递给对应的方法
     * @param param 需要传递的参数
     */
    AWorkerWrapper<P, R> setParam(P param);

}
