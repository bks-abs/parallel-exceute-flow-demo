package org.bks.abs;

import java.time.LocalDateTime;

public class Case1 {
    public static void main(String[] args) throws Exception{

        Task taskA = new Task("A"); // start
        Task taskB = new Task("B");
        Task taskD = new Task("D");
        Task taskF = new Task("F");
        Task taskG = new Task("G");
        Task taskH = new Task("H", true);// end

        taskA.addForwardDep(taskB, taskD);
        taskB.addForwardDep(taskF);
        taskD.addForwardDep(taskG);
        taskF.addForwardDep(taskH);
        taskG.addForwardDep(taskH);
        taskH.addForwardDep();

        taskA.addBackwardDep();
        taskB.addBackwardDep(taskA);
        taskD.addBackwardDep(taskA);
        taskF.addBackwardDep(taskB);
        taskG.addBackwardDep(taskD);
        taskH.addBackwardDep(taskG, taskF);

        System.out.println("MAIN start " + LocalDateTime.now());


        DependencyCheckExecutor.submitIfReadyToExecute(taskA);

        taskH.getEndNodeCountDownLatch().await();
        taskH.getEndNodeFuture().get();

        System.out.println("MAIN end " + LocalDateTime.now() + " " + taskH.isComplete());
    }
}
