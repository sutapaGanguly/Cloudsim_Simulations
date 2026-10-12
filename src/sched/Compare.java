package sched;

import java.io.PrintWriter;
import java.util.*;

public class Compare {
    static final int RUNS = 10, TASKS = 100, VMS = 20;
    static final double W_QUALITY = 0.7;       // weight of makespan in the final score (rest = speed)

    static List<Scheduler> algos(long seed) {
        return Arrays.asList(new RoundRobin(), new Greedy(),
                new MinMinMaxMin(false), new MinMinMaxMin(true),
                new GA(seed), new PSO(seed), new ACO(seed));
    }

    public static void main(String[] args) throws Exception {
        String[] names = new String[algos(0).size()];
        for (int i = 0; i < names.length; i++) names[i] = algos(0).get(i).name();
        int N = names.length;
        double[][] ms = new double[N][RUNS], ct = new double[N][RUNS],
                   imb = new double[N][RUNS], tm = new double[N][RUNS];

        // ---------- 1. run every algorithm on every workload; one chart per run ----------
        try (PrintWriter csv = new PrintWriter("results.csv")) {
            csv.println("algorithm,run,makespan,avg_completion,imbalance,sched_ms");
            for (int r = 0; r < RUNS; r++) {
                List<Scheduler> list = algos(r);               // seed = run number
                double[] runMs = new double[N];
                for (int i = 0; i < N; i++) {
                    Runner.Result x = Runner.run(list.get(i), r, TASKS, VMS);
                    ms[i][r] = x.simulated; ct[i][r] = x.avgCompletion;
                    imb[i][r] = x.imbalance; tm[i][r] = x.schedMs;
                    runMs[i] = x.simulated;
                    csv.printf(Locale.US, "%s,%d,%.4f,%.4f,%.4f,%.3f%n",
                            names[i], r + 1, x.simulated, x.avgCompletion, x.imbalance, x.schedMs);
                }
                Chart.bar("Run " + (r + 1) + ": makespan (lower is better, best outlined)",
                          names, runMs, String.format("run_%02d.png", r + 1));
                System.out.println("finished run " + (r + 1) + "/" + RUNS);
            }
        }

        // ---------- 2. summary numbers ----------
        double[] bestPerRun = new double[RUNS];
        for (int r = 0; r < RUNS; r++) {
            bestPerRun[r] = Double.MAX_VALUE;
            for (int i = 0; i < N; i++) bestPerRun[r] = Math.min(bestPerRun[r], ms[i][r]);
        }
        double[] meanRel = new double[N], meanTime = new double[N], meanImb = new double[N], meanCt = new double[N];
        int[] wins = new int[N];
        for (int i = 0; i < N; i++) {
            for (int r = 0; r < RUNS; r++) {
                double rel = ms[i][r] / bestPerRun[r];         // 1.0 = best in that run
                meanRel[i] += rel / RUNS;
                if (rel <= 1 + 1e-9) wins[i]++;
                meanTime[i] += tm[i][r] / RUNS; meanImb[i] += imb[i][r] / RUNS; meanCt[i] += ct[i][r] / RUNS;
            }
        }

        // ---------- 3. summary charts ----------
        double[] logTime = new double[N];
        for (int i = 0; i < N; i++) logTime[i] = Math.log10(1 + meanTime[i]);
        Chart.bar("Mean makespan relative to best of each run (1.0 = best)", names, meanRel, "summary_makespan.png");
        Chart.bar("Scheduling time: log10(1 + ms)", names, logTime, "summary_time.png");
        Chart.bar("Load imbalance (lower = more balanced)", names, meanImb, "summary_balance.png");
        Chart.bar("Mean task completion time", names, meanCt, "summary_completion.png");

        // ---------- 4. score and recommendation ----------
        double[] q = norm(meanRel), s = norm(logTime), score = new double[N];
        for (int i = 0; i < N; i++) score[i] = W_QUALITY * q[i] + (1 - W_QUALITY) * s[i];

        System.out.printf("%n%-12s %9s %5s %10s %8s %10s %8s%n",
                "Algorithm", "MeanRel", "Wins", "AvgCT", "Imbal", "Time ms", "Score");
        for (int i = 0; i < N; i++)
            System.out.printf("%-12s %9.3f %5d %10.1f %8.3f %10.1f %8.3f%n",
                    names[i], meanRel[i], wins[i], meanCt[i], meanImb[i], meanTime[i], score[i]);
        System.out.println("\nBest quality : " + names[argMin(meanRel)]);
        System.out.println("Fastest      : " + names[argMin(meanTime)]);
        System.out.println("Best overall : " + names[argMin(score)] + "  (quality weight " + W_QUALITY + ")");
        System.out.println("\nSaved: run_01.png ... summary_*.png, results.csv");
    }

    static double[] norm(double[] v) {                         // min-max to 0..1, lower is better
        double mn = Double.MAX_VALUE, mx = -Double.MAX_VALUE;
        for (double x : v) { mn = Math.min(mn, x); mx = Math.max(mx, x); }
        double[] o = new double[v.length];
        for (int i = 0; i < v.length; i++) o[i] = (v[i] - mn) / (mx - mn + 1e-12);
        return o;
    }
    static int argMin(double[] v) { int b = 0; for (int i = 1; i < v.length; i++) if (v[i] < v[b]) b = i; return b; }
}