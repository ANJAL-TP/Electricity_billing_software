package com.electricity.auth.ui;

import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Rounded, keyboard-accessible navigation button shared by both workspaces. */
public final class SidebarNavButton extends JButton {
    private boolean active;
    private final boolean danger;

    public SidebarNavButton(String text, Icon icon) {
        this(text, icon, false);
    }

    public SidebarNavButton(String text, Icon icon, boolean danger) {
        super(text, icon);
        this.danger = danger;
        setHorizontalAlignment(LEFT);
        setIconTextGap(14);
        setFont(UIConstants.FONT_LABEL);
        setForeground(danger ? new Color(242, 153, 156) : UIConstants.SIDEBAR_TEXT);
        setBorder(new EmptyBorder(0, 14, 0, 12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(224, 46));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Color.WHITE : danger ? new Color(242, 153, 156) : UIConstants.SIDEBAR_TEXT);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (active) {
            g.setColor(new Color(37, 99, 235, 52));
            g.fillRoundRect(0, 1, getWidth() - 1, getHeight() - 2, 10, 10);
            g.setColor(new Color(75, 145, 255));
            g.fillRoundRect(0, 9, 3, getHeight() - 18, 3, 3);
        } else if (getModel().isRollover() && !danger) {
            g.setColor(new Color(255, 255, 255, 15));
            g.fillRoundRect(0, 1, getWidth() - 1, getHeight() - 2, 10, 10);
        } else if (danger && getModel().isRollover()) {
            g.setColor(new Color(185, 48, 55, 34));
            g.fillRoundRect(0, 1, getWidth() - 1, getHeight() - 2, 10, 10);
        }
        g.dispose();
        super.paintComponent(graphics);
    }
}
