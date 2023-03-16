package top.amosen.asyncSchedule.worker;

import top.amosen.asyncSchedule.result.AWorkerResult;

import java.util.Map;

/**
 * 这个接口代表了一个具体的异步操作，是最小的一个操作单元
 *
 * @author Amosen
 * @Date 2023-03-15 17:18
 */

@FunctionalInterface
public interface AWorker<P, R> {

    /**
     * 需要执行的异步任务
     * @param param 异步任务参数
     * @param results 其他异步任务的执行结果，key为任务名，value为任务结果
     * @return 异步任务返回值
     */
    R run(P param, Map<String, AWorkerResult> results);

}
