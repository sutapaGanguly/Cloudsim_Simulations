package sched;
import java.util.Random;
public class ACO implements Scheduler {
    int ants = 30, iters = 100; double alpha = 1.0, beta = 2.0, rho = 0.1;
    Random r;
    public ACO(long seed) { r = new Random(seed); }
    public String name() { return "ACO"; }

    public int[] schedule(double[][] etc) {
        int n = etc.length, m = etc[0].length;
        double[][] tau = new double[n][m];
        for (double[] row : tau) java.util.Arrays.fill(row, 1.0);
        int[] best = null; double bestMs = Double.MAX_VALUE, Q = -1;

        for (int it = 0; it < iters; it++) {
            int[] iterBest = null; double iterMs = Double.MAX_VALUE;
            for (int a = 0; a < ants; a++) {
                double[] ready = new double[m];
                int[] map = new int[n];
                for (int t = 0; t < n; t++) {
                    double[] p = new double[m]; double sum = 0;
                    for (int v = 0; v < m; v++) {
                        double eta = 1.0 / (ready[v] + etc[t][v]);          // heuristic
                        p[v] = Math.pow(tau[t][v], alpha) * Math.pow(eta, beta);
                        sum += p[v];
                    }
                    double pick = r.nextDouble() * sum, acc = 0; int chosen = m - 1;
                    for (int v = 0; v < m; v++) { acc += p[v]; if (pick <= acc) { chosen = v; break; } }
                    map[t] = chosen; ready[chosen] += etc[t][chosen];
                }
                double ms = Metrics.makespan(map, etc);
                if (Q < 0) Q = ms;                                          // scale for deposits
                if (ms < iterMs) { iterMs = ms; iterBest = map; }
            }
            if (iterMs < bestMs) { bestMs = iterMs; best = iterBest.clone(); }
            for (int t = 0; t < n; t++)                                     // evaporation
                for (int v = 0; v < m; v++) tau[t][v] = Math.max(0.01, tau[t][v] * (1 - rho));
            for (int t = 0; t < n; t++) tau[t][best[t]] += Q / bestMs;      // deposit on best-so-far
        }
        return best;
    }
}