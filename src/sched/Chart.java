package sched;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Chart {
    private static final Color[] PALETTE = {
        new Color(31,119,180), new Color(255,127,14), new Color(44,160,44), new Color(214,39,40),
        new Color(148,103,189), new Color(140,86,75), new Color(227,119,194)};

    /** Bar chart saved as PNG. The lowest bar gets a black outline (best, since lower is better). */
    public static void bar(String title, String[] labels, double[] vals, String file) throws Exception {
        System.setProperty("java.awt.headless", "true");
        int W = 900, H = 500, L = 80, R = 30, T = 60, B = 70;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0, 0, W, H);
        g.setColor(Color.BLACK); g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(title, L, 30);

        double max = 0; int best = 0;
        for (int i = 0; i < vals.length; i++) { max = Math.max(max, vals[i]); if (vals[i] < vals[best]) best = i; }
        max = (max == 0 ? 1 : max * 1.12);
        int plotW = W - L - R, plotH = H - T - B;

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int k = 0; k <= 5; k++) {                                  // gridlines + y labels
            int y = T + plotH - (int) (plotH * k / 5.0);
            g.setColor(new Color(225, 225, 225)); g.drawLine(L, y, W - R, y);
            g.setColor(Color.BLACK); g.drawString(String.format("%.3g", max * k / 5), 8, y + 4);
        }
        int slot = plotW / vals.length, bw = slot * 2 / 3;
        for (int i = 0; i < vals.length; i++) {
            int h = (int) (plotH * vals[i] / max), x = L + i * slot + (slot - bw) / 2, y = T + plotH - h;
            g.setColor(PALETTE[i % PALETTE.length]); g.fillRect(x, y, bw, h);
            if (i == best) { g.setColor(Color.BLACK); g.setStroke(new BasicStroke(3)); g.drawRect(x, y, bw, h); }
            g.setColor(Color.BLACK); g.setStroke(new BasicStroke(1));
            String v = String.format("%.3g", vals[i]);
            g.drawString(v, x + (bw - g.getFontMetrics().stringWidth(v)) / 2, y - 5);
            g.drawString(labels[i], x + (bw - g.getFontMetrics().stringWidth(labels[i])) / 2, T + plotH + 20);
        }
        g.dispose();
        ImageIO.write(img, "png", new File(file));
    }
}
