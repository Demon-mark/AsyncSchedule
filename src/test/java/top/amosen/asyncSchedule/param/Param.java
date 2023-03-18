package top.amosen.asyncSchedule.param;

import org.junit.Test;
import top.amosen.asyncSchedule.async.Async;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.runner.Runner;
import top.amosen.asyncSchedule.scheduler.Scheduler;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Amosen
 * @Date 2023-03-16 16:04
 */
public class Param {

    @Test
    public void oneToMany() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .param("runner1 param").build();

        scheduler.newWorker("runner2", new Runner())
                .param("runner2 param").build();

        scheduler.newWorker("runner3", new Runner())
                .param("runner3 param").build();

        scheduler.must("runner1", "runner2");
        scheduler.must("runner1", "runner3");
        scheduler.run();

        Map<String, AWorkerResult> results = scheduler.results();
        results.forEach((s, result) -> System.out.println(s + ": " +result.getResult()));
    }

    @Test
    public void secure() {
        for (int i = 0; i < 100; i++) {
            manyToOne();
        }
    }

    @Test
    public void manyToOne() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .param("runner1 param").build();

        scheduler.newWorker("runner2", new Runner())
                .param("runner2 param").build();

        scheduler.newWorker("runner3", new Runner())
                .param("runner3 param").build();

        scheduler.must("runner1", "runner3");
        scheduler.must("runner2", "runner3");
        scheduler.run();

        Map<String, AWorkerResult> results = scheduler.results();
        results.forEach((s, result) -> System.out.println(s + ": " +result.getResult()));
    }

    @Test
    public void manyToMany() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .param("runner1 param").build();

        scheduler.newWorker("runner2", new Runner())
                .param("runner2 param").build();

        scheduler.newWorker("runner3", new Runner())
                .param("runner3 param").build();

        scheduler.newWorker("runner4", new Runner())
                .param("runner4 param").build();

        scheduler.must("runner1", "runner3");
        scheduler.must("runner2", "runner3");
        scheduler.must("runner1", "runner4");
        scheduler.must("runner2", "runner4");
        scheduler.run();

        Map<String, AWorkerResult> results = scheduler.results();
        results.forEach((s, result) -> System.out.println(s + ": " +result.getResult()));
    }

    @Test
    public void circle() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .param("runner1 param").build();

        scheduler.newWorker("runner2", new Runner())
                .param("runner2 param").build();

        scheduler.newWorker("runner3", new Runner())
                .param("runner3 param").build();

        scheduler.must("runner1", "runner2");
        scheduler.must("runner2", "runner3");
        scheduler.must("runner3", "runner1");
        // crack!
        scheduler.run();

    }

    @Test
    public void multi() {
        Scheduler scheduler = new Scheduler();
        for (int i = 0; i < 7; i++) {
            String name = "runner" + (i + 1);
            scheduler.newWorker(name, new Runner()).param(name + " param").build();
        }

        scheduler.must("runner1", "runner2");
        scheduler.must("runner1", "runner3");
        scheduler.must("runner2", "runner3");
        scheduler.must("runner3", "runner4");
        scheduler.must("runner4", "runner5");
        scheduler.must("runner6", "runner7");
        scheduler.must("runner7", "runner5");
        scheduler.run();

        Map<String, AWorkerResult> results = scheduler.results();
        results.forEach((s, result) -> System.out.println(s + ": " +result.getResult()));
    }
}
