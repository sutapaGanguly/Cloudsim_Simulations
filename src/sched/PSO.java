package sched;
import java.util.Random;
public class PSO implements Scheduler {
    int swarm = 40, iters = 200; double w = 0.7, c1 = 1.5, c2 = 1.5;
    Random r;
    public PSO(long seed) { r = new Random(seed); }
    public String name() { return "PSO"; }

    int[] decode(double[] x, int m) {
        int[] map = new int[x.length];
        for (int i = 0; i < x.length; i++) map[i] = (int) Math.max(0, Math.min(m - 1, Math.floor(x[i])));
        return map;
    }
    public int[] schedule(double[][] etc) {
        int n = etc.length, m = etc[0].length;
        double[][] x = new double[swarm][n], v = new double[swarm][n], pb = new double[swarm][n];
        double[] pbF = new double[swarm];
        double[] gb = new double[n]; double gbF = Double.MAX_VALUE;
        for (int i = 0; i < swarm; i++) {
            for (int t = 0; t < n; t++) { x[i][t] = r.nextDouble() * m; v[i][t] = (r.nextDouble() - 0.5) * m; }
            pb[i] = x[i].clone();
            pbF[i] = Metrics.makespan(decode(x[i], m), etc);
            if (pbF[i] < gbF) { gbF = pbF[i]; gb = x[i].clone(); }
        }
        for (int it = 0; it < iters; it++) {
            for (int i = 0; i < swarm; i++) {
                for (int t = 0; t < n; t++) {
                    v[i][t] = w * v[i][t] + c1 * r.nextDouble() * (pb[i][t] - x[i][t])
                                          + c2 * r.nextDouble() * (gb[t] - x[i][t]);
                    v[i][t] = Math.max(-m / 2.0, Math.min(m / 2.0, v[i][t]));   // velocity clamp
                    x[i][t] = Math.max(0, Math.min(m - 1e-9, x[i][t] + v[i][t]));
                }
                double fit = Metrics.makespan(decode(x[i], m), etc);
                if (fit < pbF[i]) { pbF[i] = fit; pb[i] = x[i].clone(); }
                if (fit < gbF)    { gbF = fit;    gb = x[i].clone(); }
            }
        }
        return decode(gb, m);
    }
}