package top.amosen.asyncSchedule.runner;

import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.worker.AWorker;

import java.util.Map;

/**
 * @author Amosen
 * @Date 2023-03-16 15:30
 */
public class Runner implements AWorker<String, String> {

    @Override
    public String run(String param, Map<String, AWorkerResult> results) {
        System.out.println(param);
        results.forEach((s, aWorkerResult) -> System.out.println(param + " " + s + ":" + aWorkerResult.getResult()));
        return param;
    }
}
