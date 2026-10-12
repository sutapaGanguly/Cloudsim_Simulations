package sched;
public interface Scheduler {
    String name();
    int[] schedule(double[][] etc);   // returns the VM index for every task
}