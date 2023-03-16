package top.amosen.asyncSchedule.async;

import top.amosen.asyncSchedule.scheduler.Scheduler;
import top.amosen.asyncSchedule.wrapper.AScheduleNode;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Amosen
 * @Date 2023-03-16 10:30
 */
public class Async {

    private static final ThreadPoolExecutor COMMON_POOL = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    private static ExecutorService executorService;

    public static void execute(ExecutorService executorService, Scheduler scheduler) {
        while (scheduler.hasNext()) {
            AScheduleNode node = scheduler.next();
            try {
                node.call(executorService);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void execute(Scheduler scheduler) {
        execute(COMMON_POOL, scheduler);
    }

}
