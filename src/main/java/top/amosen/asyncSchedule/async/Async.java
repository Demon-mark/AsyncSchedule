package top.amosen.asyncSchedule.async;

import top.amosen.asyncSchedule.scheduler.Scheduler;
import top.amosen.asyncSchedule.wrapper.AWorkerWrapper;

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

    public static void execute(ExecutorService service, Scheduler scheduler) {
        if (null != service) {
            executorService = service;
        } else {
            executorService = COMMON_POOL;
        }
        // 开辟异步线程来完成调度工作
        executorService.execute(() -> {
            while (scheduler.hasNext()) {
                AWorkerWrapper wrapper = scheduler.next();
                Runnable run = wrapper.execute();
                if (run != null) {
                    executorService.execute(run);
                }
            }
            try {
                scheduler.forAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
