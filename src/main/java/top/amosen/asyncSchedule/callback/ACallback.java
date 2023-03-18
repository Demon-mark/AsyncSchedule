package top.amosen.asyncSchedule.callback;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.wrapper.AWorkerWrapper;

import java.util.Map;

/**
 * 异步任务执行回调
 *
 * @author Amosen
 * @Date 2023-03-16 9:11
 */
public interface ACallback<P, R> {

    /**
     * 这个回调将在任务即将开始时调用，在这里可以有机会对参数进行最后一次检查
     * @param param 传入的参数
     */
    void onBegin(@Nullable P param);

    /**
     * 当任务有了执行结果时，这个回调会被触发，其触发时机为：当次任务正常执行之后，下个任务onBegin之前
     * @param result 当次任务的执行结果
     */
    void onResult(@Nullable R result);

    /**
     * 在当次任务出错时将会被调用
     * @param throwable 错误原因
     * @param result 执行结果的引用
     * @return 是否继续执行，返回为true将会使用result引用继续下一次任务，返回为false则会抛出异常信息终止此次任务
     */
    boolean onError(@NotNull Throwable throwable, @Nullable R result);

    /**
     * 当任务完成时调用，无论是否正常完成都会被调用
     * @param param 执行参数
     * @param result 执行结果
     * @param throwable 异常信息
     */
    void onComplete(@Nullable P param,
                    @Nullable R result,
                    @Nullable Throwable throwable);

    /**
     * 当任务呗快速失败时调用
     * @param throwable 被快速失败的原因
     * @param results 快速失败前的结果
     */
    void onFail(Throwable throwable, Map<String, AWorkerResult> results);

}
