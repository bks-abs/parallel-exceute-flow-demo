package org.bks.abs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class Task implements Runnable {
    private String name;
    private Object lock = new Object();

    // 标记节点是否执行结束 以及执行是否成功
    private boolean complete = false;
    private boolean success = false;
    // 任务是否已经提交到线程池(注意是真的被提交到线程池了)
    private boolean submitted = false;

    // 图的流程为A--->B 和 A--->C 时，则A的forwardDep为B和C，B和C的backwardDep为A
    private List<Task> forwardDep;
    private List<Task> backwardDep;

    // 标记当前节点是否为结束节点
    private boolean isEndNode = false;



    // 节点为结束节点时才会有,用于在主线程中等待
    private CountDownLatch endNodeCountDownLatch;
    // 节点为结束节点时才会有,用于在主线程中获取返回值
    private Future<?> endNodeFuture;

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, boolean isEnd) {
        this(name);
        if (isEnd) {
            isEndNode = true;
            endNodeCountDownLatch = new CountDownLatch(1);
        }
    }

    public void run() {
        // 模拟执行当前节点的业务逻辑
        try {
            System.out.println(Thread.currentThread() + " exec " + name);
            Thread.sleep(1_000);
            this.success = true;
        } catch (InterruptedException e) {
            e.printStackTrace(); // 待封装
        } finally {
            this.complete = true;
        }

        // 提交前向依赖的任务
        // 当前依赖为 A--->B 和 A--->C, 则会执行B和C
        for (Task dep : this.forwardDep) {
            Future<?> future = DependencyCheckExecutor.submitIfReadyToExecute(dep);
            // 记录下结束节点的future
            if (future != null && dep.isEndNode()) {
                dep.endNodeFuture = future;
                dep.endNodeCountDownLatch.countDown();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }


    public List<Task> getForwardDep() {
        return forwardDep;
    }

    public void setForwardDep(List<Task> forwardDep) {
        this.forwardDep = forwardDep;
    }

    public List<Task> getBackwardDep() {
        return backwardDep;
    }

    public void setBackwardDep(List<Task> backwardDep) {
        this.backwardDep = backwardDep;
    }

    public void addForwardDep(Task... tasks) {
        if(this.forwardDep == null){
            this.forwardDep = new ArrayList<>();
        }
        for (Task task : tasks) {
            this.forwardDep.add(task);
        }
    }


    public void addBackwardDep(Task... tasks) {
        if(this.backwardDep == null){
            this.backwardDep = new ArrayList<>();
        }
        for (Task task : tasks) {
            this.backwardDep.add(task);
        }
    }

    public boolean isEndNode() {
        return isEndNode;
    }

    public void setEndNode(boolean endNode) {
        isEndNode = endNode;
    }

    public Object getLock() {
        return lock;
    }

    public void setLock(Object lock) {
        this.lock = lock;
    }

    public CountDownLatch getEndNodeCountDownLatch() {
        return endNodeCountDownLatch;
    }

    public void setEndNodeCountDownLatch(CountDownLatch endNodeCountDownLatch) {
        this.endNodeCountDownLatch = endNodeCountDownLatch;
    }

    public Future<?> getEndNodeFuture() {
        return endNodeFuture;
    }

    public void setEndNodeFuture(Future<?> endNodeFuture) {
        this.endNodeFuture = endNodeFuture;
    }


    @Override
    public String toString() {
        return "Task:" + name;
    }
}