package sched;
public class RoundRobin implements Scheduler {
    public String name() { return "RoundRobin"; }
    public int[] schedule(double[][] etc) {
        int n = etc.length, m = etc[0].length;
        int[] map = new int[n];
        for (int t = 0; t < n; t++) map[t] = t % m;
        return map;
    }
}