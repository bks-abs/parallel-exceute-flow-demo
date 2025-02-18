package org.bks.abs;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DependencyCheckExecutor {
    private static ExecutorService EX = Executors.newFixedThreadPool(9);

    /**
     * 根据依赖关系决定是否提交到线程池中执行
     *
     * A-->C
     * B-->C
     * 若当前Task是C,该方法保证C仅执行1次
     * @param task
     * @return
     */
    public static Future<?> submitIfReadyToExecute(Task task) {
        if (task == null) {
            throw new IllegalStateException("task can not be null");
        }

        // 无前置依赖节点,则直接提交
        List<Task> backwardDep = task.getBackwardDep();
        if (backwardDep == null || backwardDep.isEmpty()) {
            return EX.submit(task);
        } else {
            // 如果所有的前置依赖节点都执行完毕了,则执行当前节点
            int size = backwardDep.size();
            synchronized (task.getLock()) {
                for (Task dep : backwardDep) {
                    if (dep.isComplete()) {
                        size--;
                    }
                }
                if (size <= 0 && (!task.isSubmitted())) {
                    Future<?> f = EX.submit(task);
                    task.setSubmitted(true);
                    return f;
                } else {
                    System.out.println(task + " backward dependencies[" + backwardDep +
                            "] are not fully completed, dependencies = ");
                    return null;
                }
            }
        }
    }

}
