package sched;
public class Greedy implements Scheduler {
    public String name() { return "Greedy"; }
    public int[] schedule(double[][] etc) {
        int n = etc.length, m = etc[0].length;
        double[] ready = new double[m];
        int[] map = new int[n];
        for (int t = 0; t < n; t++) {
            int best = 0; double bestCt = Double.MAX_VALUE;
            for (int v = 0; v < m; v++) {
                double ct = ready[v] + etc[t][v];
                if (ct < bestCt) { bestCt = ct; best = v; }
            }
            map[t] = best; ready[best] += etc[t][best];
        }
        return map;
    }
}