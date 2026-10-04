package com.electricity.admindashboard;

import com.electricity.admindashboard.service.MonthlyRevenue;
import com.electricity.admindashboard.service.PaymentMethodSummary;
import com.electricity.admindashboard.service.ReportsService;
import com.electricity.admindashboard.service.ReportsSummary;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/** Data-backed consumer, billing, payments and monthly revenue reports. */
public final class ReportsPanel extends JPanel {
    private final ReportsService service;
    private final JPanel metrics = new JPanel(new GridLayout(2, 4, 12, 12));
    private final DefaultTableModel monthlyModel = new DefaultTableModel(new Object[]{
            "Month", "Bills", "Total Billed", "Total Paid", "Outstanding"
    }, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
    private final DefaultTableModel methodModel = new DefaultTableModel(new Object[]{"Payment Method", "Payments", "Paid Amount"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public ReportsPanel(ReportsService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 18));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(titleBar(), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);
        refreshData();
    }

    private JComponent titleBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("ADMINISTRATOR  /  INSIGHTS");
        eyebrow.setFont(UIConstants.FONT_BADGE);
        eyebrow.setForeground(UIConstants.TEXT_MUTED);
        JLabel title = new JLabel("Reports");
        title.setFont(UIConstants.FONT_PAGE_TITLE);
        title.setForeground(UIConstants.TEXT_DARK);
        JLabel subtitle = new JLabel("Monitor billing, collections, and consumer account activity.");
        subtitle.setFont(UIConstants.FONT_SUBTITLE);
        subtitle.setForeground(UIConstants.TEXT_MUTED);
        copy.add(eyebrow);
        copy.add(Box.createVerticalStrut(4));
        copy.add(title);
        copy.add(Box.createVerticalStrut(3));
        copy.add(subtitle);
        JButton refresh = new JButton("Refresh Reports");
        UIConstants.styleButton(refresh, Color.WHITE, UIConstants.PRIMARY_NAVY);
        refresh.addActionListener(event -> refreshData());
        panel.add(copy, BorderLayout.WEST);
        panel.add(refresh, BorderLayout.EAST);
        return panel;
    }

    private JComponent content() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        metrics.setOpaque(false);
        JPanel tables = new JPanel(new GridLayout(1, 2, 14, 0));
        tables.setOpaque(false);
        JTable monthly = new JTable(monthlyModel);
        JTable methods = new JTable(methodModel);
        style(monthly); style(methods);
        tables.add(tableCard("Monthly Revenue", monthly));
        tables.add(tableCard("Successful Payments by Method", methods));
        panel.add(metrics, BorderLayout.NORTH);
        panel.add(tables, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        ReportsSummary summary = service.loadReports();
        metrics.removeAll();
        metrics.add(metric("TOTAL CONSUMERS", String.valueOf(summary.totalConsumers()),
                "Active " + summary.activeConsumers() + " · Inactive " + summary.inactiveConsumers()));
        metrics.add(metric("TOTAL BILLS", String.valueOf(summary.totalBills()), "All billing periods"));
        metrics.add(metric("TOTAL BILLED", money(summary.totalBilled()), "Gross billed amount"));
        metrics.add(metric("PAID AMOUNT", money(summary.paidAmount()), "Successful payments"));
        metrics.add(metric("OUTSTANDING", money(summary.outstandingAmount()), "Remaining bill balances"));
        metrics.add(metric("PAYMENT RECORDS", String.valueOf(summary.totalPayments()), "Includes pending requests"));
        metrics.add(metric("ACTIVE CONSUMERS", String.valueOf(summary.activeConsumers()), "Currently active accounts"));
        metrics.add(metric("INACTIVE CONSUMERS", String.valueOf(summary.inactiveConsumers()), "Deactivated accounts"));
        monthlyModel.setRowCount(0);
        for (MonthlyRevenue row : summary.monthlyRevenue()) monthlyModel.addRow(new Object[]{row.billingPeriod(), row.billCount(),
                money(row.billed()), money(row.paid()), money(row.outstanding())});
        methodModel.setRowCount(0);
        for (PaymentMethodSummary row : summary.paymentMethods()) methodModel.addRow(new Object[]{row.method(), row.paymentCount(), money(row.paidAmount())});
        revalidate(); repaint();
    }

    private static JPanel metric(String title, String value, String detail) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 11));
        heading.setForeground(UIConstants.TEXT_MUTED);
        JLabel number = new JLabel(value);
        number.setFont(new Font("Segoe UI", Font.BOLD, 20));
        number.setForeground(UIConstants.PRIMARY_NAVY);
        JLabel caption = new JLabel(detail);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        caption.setForeground(UIConstants.TEXT_MUTED);
        card.add(heading); card.add(Box.createVerticalStrut(8)); card.add(number); card.add(Box.createVerticalStrut(4)); card.add(caption);
        return card;
    }

    private static JPanel tableCard(String title, JTable table) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(heading, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private static void style(JTable table) {
        table.setAutoCreateRowSorter(true);
        UIConstants.styleTable(table);
    }

    private static String money(java.math.BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).format(value);
    }
}
