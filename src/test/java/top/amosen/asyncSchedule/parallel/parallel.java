package top.amosen.asyncSchedule.parallel;

import org.junit.Test;
import top.amosen.asyncSchedule.async.Async;
import top.amosen.asyncSchedule.callback.CallBack;
import top.amosen.asyncSchedule.runner.Runner;
import top.amosen.asyncSchedule.scheduler.Scheduler;
import top.amosen.asyncSchedule.wrapper.AScheduleNode;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * @author Amosen
 * @Date 2023-03-16 15:29
 */
public class parallel {

   @Test
   public void parallel() {
      Scheduler scheduler = new Scheduler();
      scheduler.newWorker("runner1", new Runner())
              .setParam("runner1")
              .callback(new CallBack("runner1"));

      scheduler.newWorker("runner2", new Runner())
              .setParam("runner2")
              .callback(new CallBack("runner2"));
      scheduler.newWorker("runner3", new Runner())
              .setParam("runner3")
              .callback(new CallBack("runner3"));

      Async.execute(scheduler);

      Iterator<AScheduleNode> results = scheduler.results();
      while (results.hasNext()) {
         try {
            System.out.println(results.next().result().getResult());
         } catch (ExecutionException e) {
            throw new RuntimeException(e);
         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         }
      }


   }

}
