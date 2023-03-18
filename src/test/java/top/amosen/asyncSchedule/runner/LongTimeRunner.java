package top.amosen.asyncSchedule.runner;

import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;

import java.util.Map;

/**
 * @author Amosen
 * @Date 2023-03-18 18:34
 */
public class LongTimeRunner implements AWorker<String, String> {
    @Override
    public String run(String param, Map<String, AWorkerResult> results) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
