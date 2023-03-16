# asyncSchedule 异步任务编排框架
借鉴于京东asyncTool框架，简化异步任务编排API

## 相关概念
+ AWorker：最小工作单元，提供具体的异步任务
+ AWorkerWrapper：最小执行单元，为异步任务提供回调能力，传递异步任务参数
+ AWorkerNode：最小调度单元，提供自适应的调度能力
+ Scheduler：与AWorkerNode配合通过三色标记法实现不同任务之间的依赖关系
+ Async：线程池包装，与Scheduler配合完成异步任务及其编排

## 特点
+ API设计符合开发人员常规思考方式
+ 灵活的事件回调
+ 相互依赖的任务之间结果互相传递
+ 支持并行、一对多、多对一、多对多的任务编排

## 相关接口
+ AWorker：覆写这个接口的run方法来定义自己的业务逻辑，param参数代表了未来会传递给这个接口的参数，results则代表了可能的其他任务为该任务提供的结果
+ Scheduler.newWorker(String name, AWorker worker)：通过这个接口来告知调度器需要调度的worker，不要忘记为你的任务提供参数（.setParam()）
+ Scheduler.resultTransfer(String from, String to)：通过这个接口，调度器将知道数据需要从名字为from的worker传递到名字为to的worker，当调用这个接口时，需要保证from，to所代表的worker已经被正确实例化（未来可能提供预配置来使调度器能够调度未来的worker）
+ Async.execute(Scheduler scheduler)：运行你的任务

## 示例代码
+ 并行执行
```java
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
```

+ 一对多数据传递
```java
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
}
```
+ 多对一数据传递
```java
public class Param {
    
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
}
```
+ 多对多数据传递
```java
public class Param {
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
}
```