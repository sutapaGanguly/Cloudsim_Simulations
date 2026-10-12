package sched;
public final class Metrics {
    public static double makespan(int[] map, double[][] etc) {
        double[] load = new double[etc[0].length];
        for (int t = 0; t < map.length; t++) load[map[t]] += etc[t][map[t]];
        double max = 0;
        for (double l : load) max = Math.max(max, l);
        return max;
    }
}