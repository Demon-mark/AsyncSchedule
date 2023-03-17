package top.amosen.asyncSchedule.param;

import org.junit.Test;
import top.amosen.asyncSchedule.async.Async;
import top.amosen.asyncSchedule.runner.Runner;
import top.amosen.asyncSchedule.scheduler.Scheduler;

/**
 * @author Amosen
 * @Date 2023-03-16 16:04
 */
public class Param {

    @Test
    public void oneToMany() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .setParam("runner1 param");

        scheduler.newWorker("runner2", new Runner())
                .setParam("runner2 param");

        scheduler.newWorker("runner3", new Runner())
                .setParam("runner3 param");

        scheduler.resultTransfer("runner1", "runner2");
        scheduler.resultTransfer("runner1", "runner3");
        Async.execute(scheduler);

    }

    @Test
    public void manyToOne() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .setParam("runner1 param");

        scheduler.newWorker("runner2", new Runner())
                .setParam("runner2 param");

        scheduler.newWorker("runner3", new Runner())
                .setParam("runner3 param");

        scheduler.resultTransfer("runner1", "runner3");
        scheduler.resultTransfer("runner2", "runner3");
        Async.execute(scheduler);

    }

    @Test
    public void manyToMany() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .setParam("runner1 param");

        scheduler.newWorker("runner2", new Runner())
                .setParam("runner2 param");

        scheduler.newWorker("runner3", new Runner())
                .setParam("runner3 param");

        scheduler.newWorker("runner4", new Runner())
                .setParam("runner4 param");

        scheduler.resultTransfer("runner1", "runner3");
        scheduler.resultTransfer("runner2", "runner3");
        scheduler.resultTransfer("runner1", "runner4");
        scheduler.resultTransfer("runner2", "runner4");
        Async.execute(scheduler);

    }

    @Test
    public void circle() {
        Scheduler scheduler = new Scheduler();
        scheduler.newWorker("runner1", new Runner())
                .setParam("runner1 param");

        scheduler.newWorker("runner2", new Runner())
                .setParam("runner2 param");

        scheduler.newWorker("runner3", new Runner())
                .setParam("runner3 param");

        scheduler.resultTransfer("runner1", "runner2");
        scheduler.resultTransfer("runner2", "runner3");
        scheduler.resultTransfer("runner3", "runner1");
        Async.execute(scheduler);

    }

    @Test
    public void multi() {
        Scheduler scheduler = new Scheduler();
        for (int i = 0; i < 7; i++) {
            String name = "runner" + (i + 1);
            scheduler.newWorker(name, new Runner()).setParam(name + " param");
        }

        scheduler.resultTransfer("runner1", "runner2");
        scheduler.resultTransfer("runner1", "runner3");
        scheduler.resultTransfer("runner2", "runner3");
        scheduler.resultTransfer("runner3", "runner4");
        scheduler.resultTransfer("runner4", "runner5");
        scheduler.resultTransfer("runner6", "runner7");
        scheduler.resultTransfer("runner7", "runner5");
        Async.execute(scheduler);
    }
}
