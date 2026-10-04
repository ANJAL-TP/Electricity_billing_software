package com.electricity.auth.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/** Small, consistent line icons for desktop navigation and KPI cards. */
public final class UIIcons {
    private UIIcons() { }

    public enum Kind { DASHBOARD, CONSUMERS, METER, BILL, HISTORY, PAYMENT, REPORTS, PROFILE, LOGOUT, LIGHTNING }

    public static Icon of(Kind kind) {
        return new LineIcon(kind);
    }

    private record LineIcon(Kind kind) implements Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = component.getForeground() == null ? UIConstants.TEXT_MUTED : component.getForeground();
            g.setColor(color);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (kind) {
                case DASHBOARD -> dashboard(g);
                case CONSUMERS -> consumers(g);
                case METER -> meter(g);
                case BILL -> bill(g);
                case HISTORY -> history(g);
                case PAYMENT -> payment(g);
                case REPORTS -> reports(g);
                case PROFILE -> profile(g);
                case LOGOUT -> logout(g);
                case LIGHTNING -> lightning(g);
            }
            g.dispose();
        }

        private static void dashboard(Graphics2D g) {
            g.drawRoundRect(1, 1, 7, 7, 2, 2);
            g.drawRoundRect(10, 1, 7, 7, 2, 2);
            g.drawRoundRect(1, 10, 7, 7, 2, 2);
            g.drawRoundRect(10, 10, 7, 7, 2, 2);
        }

        private static void consumers(Graphics2D g) {
            g.drawOval(6, 1, 6, 6);
            g.drawArc(3, 8, 12, 9, 0, 180);
            g.drawArc(0, 6, 8, 8, 35, 120);
            g.drawArc(10, 6, 8, 8, 25, 120);
        }

        private static void meter(Graphics2D g) {
            g.drawRoundRect(2, 1, 14, 16, 3, 3);
            g.drawOval(5, 4, 8, 8);
            g.drawLine(9, 8, 12, 6);
            g.drawLine(5, 14, 13, 14);
        }

        private static void bill(Graphics2D g) {
            Path2D page = new Path2D.Float();
            page.moveTo(4, 1); page.lineTo(11, 1); page.lineTo(15, 5); page.lineTo(15, 17);
            page.lineTo(4, 17); page.closePath();
            g.draw(page);
            g.drawLine(11, 1, 11, 5); g.drawLine(11, 5, 15, 5);
            g.drawLine(6, 9, 13, 9); g.drawLine(6, 12, 13, 12); g.drawLine(6, 15, 11, 15);
        }

        private static void history(Graphics2D g) {
            g.drawOval(1, 1, 16, 16);
            g.drawLine(9, 4, 9, 9); g.drawLine(9, 9, 13, 11);
        }

        private static void payment(Graphics2D g) {
            g.drawOval(1, 2, 14, 14);
            g.drawOval(4, 4, 14, 14);
            g.drawLine(9, 7, 13, 7); g.drawLine(11, 6, 11, 13);
            g.drawArc(8, 8, 6, 5, 180, 180);
        }

        private static void reports(Graphics2D g) {
            g.drawLine(2, 16, 17, 16);
            g.drawRoundRect(3, 10, 3, 6, 1, 1);
            g.drawRoundRect(8, 6, 3, 10, 1, 1);
            g.drawRoundRect(13, 2, 3, 14, 1, 1);
        }

        private static void profile(Graphics2D g) {
            g.drawOval(6, 1, 6, 6);
            g.drawArc(3, 8, 12, 9, 0, 180);
        }

        private static void logout(Graphics2D g) {
            g.drawRoundRect(2, 2, 8, 14, 2, 2);
            g.drawLine(7, 9, 17, 9);
            g.drawLine(13, 5, 17, 9); g.drawLine(13, 13, 17, 9);
        }

        private static void lightning(Graphics2D g) {
            Path2D bolt = new Path2D.Float();
            bolt.moveTo(10, 0); bolt.lineTo(4, 10); bolt.lineTo(8, 10);
            bolt.lineTo(6, 18); bolt.lineTo(14, 7); bolt.lineTo(10, 7); bolt.closePath();
            g.fill(bolt);
        }
    }
}
