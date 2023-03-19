package top.amosen.asyncSchedule.param;

import org.junit.Test;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.runner.ErrorRunner;
import top.amosen.asyncSchedule.runner.LongTimeRunner;
import top.amosen.asyncSchedule.runner.Runner;
import top.amosen.asyncSchedule.scheduler.Scheduler;

import java.util.Map;

/**
 * @author Amosen
 * @Date 2023-03-18 17:46
 */
public class MustNeed {

    @Test
    public void mustTest() throws InterruptedException {

        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .param("runner1 param").build();

        scheduler.newWorker("runner2", new Runner())
                .param("runner2 param").build();

        scheduler.newWorker("runner3", new Runner())
                .param("runner3 param").build();

        scheduler.need("runner1", "runner3");
        scheduler.need("runner2", "runner3");
        scheduler.run();

        // blocking
        scheduler.results();

    }

    @Test
    public void mustFastFailTest() throws InterruptedException {

        System.out.println(System.currentTimeMillis());

        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .param("runner1 param").build();

        scheduler.newWorker("runner2", new ErrorRunner())
                .param("runner2 param").build();

        scheduler.newWorker("runner3", new LongTimeRunner())
                .param("runner3 param").build();

        scheduler.must("runner1", "runner3");
        scheduler.must("runner2", "runner3");
        scheduler.run();

        // blocking
        Map<String, AWorkerResult> results = scheduler.results();

        System.out.println(System.currentTimeMillis());

    }

    @Test
    public void needFastFailTest() throws InterruptedException {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .param("runner1 param").build();

        scheduler.newWorker("runner2", new ErrorRunner())
                .param("runner2 param").build();

        scheduler.newWorker("runner3", new Runner())
                .param("runner3 param").build();

        scheduler.need("runner1", "runner3");
        scheduler.need("runner2", "runner3");
        scheduler.run();

        // blocking
        Map<String, AWorkerResult> results = scheduler.results();
    }

}
