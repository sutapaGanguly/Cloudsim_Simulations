package sched;
public class MinMinMaxMin implements Scheduler {
    private final boolean maxMin;
    public MinMinMaxMin(boolean maxMin) { this.maxMin = maxMin; }
    public String name() { return maxMin ? "Max-Min" : "Min-Min"; }

    public int[] schedule(double[][] etc) {
        int n = etc.length, m = etc[0].length;
        double[] ready = new double[m];
        boolean[] done = new boolean[n];
        int[] map = new int[n];
        for (int k = 0; k < n; k++) {
            int pickT = -1, pickV = -1;
            double pickCt = maxMin ? -1 : Double.MAX_VALUE;
            for (int t = 0; t < n; t++) {
                if (done[t]) continue;
                // best VM (minimum completion time) for this task
                int bv = 0; double bct = Double.MAX_VALUE;
                for (int v = 0; v < m; v++) {
                    double ct = ready[v] + etc[t][v];
                    if (ct < bct) { bct = ct; bv = v; }
                }
                // Min-Min picks smallest of the minimums, Max-Min picks largest
                boolean better = maxMin ? bct > pickCt : bct < pickCt;
                if (better) { pickCt = bct; pickT = t; pickV = bv; }
            }
            done[pickT] = true; map[pickT] = pickV; ready[pickV] += etc[pickT][pickV];
        }
        return map;
    }
}