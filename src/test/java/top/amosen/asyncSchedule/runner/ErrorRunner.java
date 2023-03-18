package top.amosen.asyncSchedule.runner;

import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;

import java.util.Map;

/**
 * @author Amosen
 * @Date 2023-03-18 17:46
 */
public class ErrorRunner implements AWorker<String, String> {
    @Override
    public String run(String param, Map<String, AWorkerResult> results) {
        throw new RuntimeException("Exception!!!");
    }
}
