package top.amosen.asyncSchedule.parallel;

import org.junit.Test;
import top.amosen.asyncSchedule.async.Async;
import top.amosen.asyncSchedule.callback.DefaultCallbackAdapter;
import top.amosen.asyncSchedule.result.AWorkerResult;
import top.amosen.asyncSchedule.runner.Runner;
import top.amosen.asyncSchedule.scheduler.Scheduler;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

/**
 * @author Amosen
 * @Date 2023-03-16 15:29
 */
public class parallel {

   @Test
   public void parallel() {

      Scheduler scheduler = new Scheduler();

      for (int i = 0; i < 50; i++) {
         scheduler.newWorker("runner" + i, new Runner())
                 .param("runner" + i)
                 .callback(new DefaultCallbackAdapter<String, String>() {
                    @Override
                    public void onResult(String result) {
                       System.out.println("result" + result);
                    }

                    @Override
                    public boolean onError(Throwable throwable, String result) {
                       throwable.printStackTrace();
                       return true;
                    }
                 }).build();
      }

      scheduler.run();

       Map<String, AWorkerResult> results = scheduler.results();
       results.forEach(new BiConsumer<String, AWorkerResult>() {
           @Override
           public void accept(String s, AWorkerResult result) {
               System.out.println(s + ": " + result.getResult());
           }
       });

   }

}
