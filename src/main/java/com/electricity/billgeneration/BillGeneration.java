package com.electricity.billgeneration;

import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Main Standalone Application Window for Module 6 — Bill Generation.
 */
public class BillGeneration extends JFrame {

    private final BillGenerationPanel billPanel;

    public BillGeneration() {
        setTitle("Electricity Billing System — Bill Generation Module");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        getContentPane().setBackground(UIConstants.BG_LIGHT);
        setLayout(new BorderLayout());

        billPanel = new BillGenerationPanel();
        add(billPanel, BorderLayout.CENTER);
    }

    private Image createAppIcon() {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UIConstants.PRIMARY_NAVY);
        g2.fillOval(2, 2, size - 4, size - 4);
        g2.setColor(new Color(250, 204, 21)); // Gold bolt
        Polygon bolt = new Polygon();
        bolt.addPoint(17, 6);
        bolt.addPoint(11, 17);
        bolt.addPoint(16, 17);
        bolt.addPoint(14, 26);
        bolt.addPoint(22, 14);
        bolt.addPoint(17, 14);
        g2.fillPolygon(bolt);
        g2.dispose();
        return image;
    }

    public BillGenerationPanel getBillPanel() {
        return billPanel;
    }

    /**
     * Standalone main method allowing direct execution of the Bill Generation window.
     */
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            BillGeneration window = new BillGeneration();
            window.setVisible(true);
        });
    }
}
