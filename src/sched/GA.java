package sched;
import java.util.Random;
public class GA implements Scheduler {
    int pop = 60, gens = 200; double cx = 0.8, mut = 0.02;
    Random r;
    public GA(long seed) { r = new Random(seed); }
    public String name() { return "GA"; }

    public int[] schedule(double[][] etc) {
        int n = etc.length, m = etc[0].length;
        int[][] P = new int[pop][n]; double[] f = new double[pop];
        for (int i = 0; i < pop; i++) {
            for (int t = 0; t < n; t++) P[i][t] = r.nextInt(m);
            f[i] = Metrics.makespan(P[i], etc);
        }
        for (int g = 0; g < gens; g++) {
            int[][] N = new int[pop][];
            N[0] = P[argMin(f)].clone();                 // elitism
            for (int i = 1; i < pop; i++) {
                int[] a = tournament(P, f), b = tournament(P, f), c = a.clone();
                if (r.nextDouble() < cx) {               // one-point crossover
                    int cut = r.nextInt(n);
                    for (int t = cut; t < n; t++) c[t] = b[t];
                }
                for (int t = 0; t < n; t++)              // mutation
                    if (r.nextDouble() < mut) c[t] = r.nextInt(m);
                N[i] = c;
            }
            P = N;
            for (int i = 0; i < pop; i++) f[i] = Metrics.makespan(P[i], etc);
        }
        return P[argMin(f)].clone();
    }
    int[] tournament(int[][] P, double[] f) {
        int best = r.nextInt(pop);
        for (int k = 0; k < 2; k++) { int c = r.nextInt(pop); if (f[c] < f[best]) best = c; }
        return P[best];
    }
    int argMin(double[] f) { int b = 0; for (int i = 1; i < f.length; i++) if (f[i] < f[b]) b = i; return b; }
}