package top.amosen.asyncSchedule.scheduler;

import top.amosen.asyncSchedule.worker.AWorker;
import top.amosen.asyncSchedule.wrapper.AScheduleNode;
import top.amosen.asyncSchedule.wrapper.AWorkerWrapper;
import top.amosen.asyncSchedule.wrapper.DefaultWorkerWrapper;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 调度，单线程处理
 * @author Amosen
 * @Date 2023-03-16 14:11
 */
public class Scheduler implements Iterator {

    private final BlockingDeque<AScheduleNode> executable = new LinkedBlockingDeque<>();

    private final Set<AScheduleNode> waiting = Collections.synchronizedSet(new HashSet<>());

    private final Set<AScheduleNode> executed = Collections.synchronizedSet(new HashSet<>());

    private final Set<AScheduleNode> executing = Collections.synchronizedSet(new HashSet<>());

    private final Map<String, AScheduleNode> registered = new HashMap<>();

    public boolean hasNext() {
        if (executable.isEmpty() && !waiting.isEmpty() && executing.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder("[ ");
            waiting.forEach(node -> stringBuilder.append(node.getName() + ", "));
            stringBuilder.append("]");
            throw new RuntimeException("circular transfer detected between worker: " + stringBuilder.toString());
        }
        return !executable.isEmpty() || !waiting.isEmpty();
    }

    public AScheduleNode next() {
        try {
            AScheduleNode node = executable.take();
            executing.add(node);
            return node;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // when executing
    public void removeWaiting(AScheduleNode node) {
        waiting.remove(node);
        executable.offer(node);
    }

    // when init
    public void addWaiting(AScheduleNode node) {
        if (executable.contains(node)) {
            executable.remove(node);
        }
        waiting.add(node);
    }

    public void addExecutable(AScheduleNode node) {
        executable.add(node);
    }

    public void addExecuted(AScheduleNode node) {
        executing.remove(node);
        executed.add(node);
    }

    public <P, R> AWorkerWrapper<P, R> newWorker(String name, AWorker<P, R> worker) {
        AWorkerWrapper<P, R> wrapper = new DefaultWorkerWrapper<>();
        wrapper.setWorker(worker);
        AScheduleNode<P, R> node = new AScheduleNode<>(this);
        node.setWrapper(wrapper);
        node.setName(name);
        registered.put(name, node);
        return wrapper;
    }

    /**
     * 指定from的任务结果传递给to
     * @param from 结果来源
     * @param to 结果传递目标
     */
    public void resultTransfer(String from, String to) {
        if (registered.get(from) == null || registered.get(to) == null) {
            throw new RuntimeException("please initial worker before attach");
        }
        registered.get(from).setNextNode(registered.get(to));
    }

    public void resultTransfer(String from, String ... to) {
        for (String worker : to) {
            resultTransfer(from, worker);
        }
    }

    /**
     * 获取最终所有异步任务的执行结果
     * @return
     */
    public Iterator<AScheduleNode> results() {
        while (registered.size() != executed.size()) {
        }
        return executed.iterator();
    }

}
