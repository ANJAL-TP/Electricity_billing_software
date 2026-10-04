package com.electricity.auth.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.JTableHeader;

/**
 * Design system constants (colors, fonts, borders) for the Electricity Billing System UI.
 */
public final class UIConstants {

    private UIConstants() {}

    // Color Palette
    public static final Color PRIMARY_NAVY = new Color(20, 45, 72);       // #142D48
    public static final Color ACCENT_BLUE  = new Color(37, 99, 235);      // #2563EB
    public static final Color HOVER_BLUE   = new Color(29, 78, 216);      // #1D4ED8
    public static final Color TEXT_DARK    = new Color(15, 23, 42);        // #0F172A
    public static final Color TEXT_MUTED   = new Color(91, 106, 124);      // #5B6A7C
    public static final Color BORDER_LIGHT = new Color(220, 228, 237);     // #DCE4ED
    public static final Color BG_LIGHT     = new Color(244, 247, 251);     // #F4F7FB
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color SIDEBAR_BG   = new Color(15, 34, 55);        // #0F2237
    public static final Color SIDEBAR_TEXT = new Color(194, 207, 221);     // #C2CFDD
    public static final Color SUCCESS_GREEN = new Color(22, 127, 83);      // #167F53
    public static final Color ERROR_RED    = new Color(185, 48, 55);       // #B93037
    public static final Color CLEAR_GRAY   = new Color(100, 116, 139);     // #64748B
    public static final Color CLEAR_HOVER  = new Color(71, 85, 105);       // #475569
    public static final Color EXIT_RED     = new Color(185, 48, 55);       // #B93037
    public static final Color EXIT_HOVER   = new Color(153, 27, 40);       // #991B28

    // Typography
    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 21);
    public static final Font FONT_PAGE_TITLE   = new Font("Segoe UI", Font.BOLD, 25);
    public static final Font FONT_SECTION_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_INPUT        = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MESSAGE      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BADGE        = new Font("Segoe UI", Font.BOLD, 11);

    /** Applies a consistent baseline to native Swing controls before a window is constructed. */
    public static void installDefaults() {
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("ToggleButton.font", FONT_LABEL);
        UIManager.put("Label.font", FONT_MESSAGE);
        UIManager.put("TextField.font", FONT_INPUT);
        UIManager.put("PasswordField.font", FONT_INPUT);
        UIManager.put("ComboBox.font", FONT_INPUT);
        UIManager.put("Table.font", FONT_MESSAGE);
        UIManager.put("Table.foreground", TEXT_DARK);
        UIManager.put("Table.background", CARD_BG);
        UIManager.put("Table.selectionBackground", new Color(226, 237, 252));
        UIManager.put("Table.selectionForeground", TEXT_DARK);
        UIManager.put("Table.gridColor", BORDER_LIGHT);
        UIManager.put("TableHeader.font", FONT_LABEL);
        UIManager.put("TableHeader.background", new Color(247, 249, 252));
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("OptionPane.messageFont", FONT_MESSAGE);
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);
        UIManager.put("ToolTip.font", FONT_MESSAGE);
    }

    /** Shared table treatment used across the admin and consumer workspaces. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_MESSAGE);
        table.setForeground(TEXT_DARK);
        table.setBackground(CARD_BG);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_LIGHT);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(226, 237, 252));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_LABEL);
        header.setForeground(TEXT_MUTED);
        header.setBackground(new Color(247, 249, 252));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setBorder(new CompoundBorder(new LineBorder(BORDER_LIGHT, 1), new EmptyBorder(0, 6, 0, 6)));
    }

    /** Shared input treatment for editable text fields. */
    public static void styleTextField(JTextField field) {
        field.setFont(FONT_INPUT);
        field.setForeground(TEXT_DARK);
        field.setBackground(CARD_BG);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 40));
    }

    /** Shared button sizing and focus behavior; callers supply semantic colors. */
    public static void styleButton(JButton button, Color background, Color foreground) {
        // Use Swing's portable painter so configured colors remain visible under
        // Windows' native look and feel as well as the default cross-platform LAF.
        button.setUI(new BasicButtonUI());
        button.setFont(FONT_BUTTON);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(background.equals(CARD_BG) ? BORDER_LIGHT : background, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
    }
}
