package com.electricity.admindashboard;

import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.model.DashboardStats;
import com.electricity.admindashboard.model.RecentActivity;
import com.electricity.admindashboard.service.AdminDashboardService;
import com.electricity.auth.ui.UIConstants;
import com.electricity.auth.ui.UIIcons;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Main Dashboard Home panel showing KPI statistic cards and a recent activity feed.
 */
public class DashboardPanel extends JPanel {

    private final AdminDashboardService dashboardService;
    private final AdminProfile profile;

    // Stat labels for refresh capability
    private JLabel lblTotalConsumersVal;
    private JLabel lblTotalBillsVal;
    private JLabel lblPaidBillsVal;
    private JLabel lblPaidRate;
    private JLabel lblUnpaidBillsVal;
    private JLabel lblTotalRevenueVal;
    private DefaultTableModel activityTableModel;

    public DashboardPanel(AdminDashboardService dashboardService, AdminProfile profile) {
        this.dashboardService = dashboardService;
        this.profile = profile;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);

        // Build UI inside a responsive scroll pane
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBackground(UIConstants.BG_LIGHT);
        contentContainer.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Header & Greeting Bar
        contentContainer.add(createHeaderBar());
        contentContainer.add(Box.createVerticalStrut(20));

        // 2. 5 KPI Statistic Cards
        contentContainer.add(createStatCardsPanel());
        contentContainer.add(Box.createVerticalStrut(24));

        // 3. Recent Activity Section
        contentContainer.add(createRecentActivitySection());
        contentContainer.add(Box.createVerticalStrut(20));

        // 4. Operational Summary Banner
        contentContainer.add(createOperationalNoticeBanner());

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblBreadcrumb = new JLabel("ADMINISTRATOR  /  OVERVIEW");
        lblBreadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBreadcrumb.setForeground(UIConstants.TEXT_MUTED);

        String greetingName = (profile != null && profile.fullName() != null)
                ? profile.fullName()
                : "Administrator";
        JLabel lblHeading = new JLabel("Welcome back, " + greetingName);
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeading.setForeground(UIConstants.TEXT_DARK);

        titleBox.add(lblBreadcrumb);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblHeading);

        // Right side date/status badge
        JPanel rightBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rightBadge.setOpaque(false);

        JLabel lblSystemStatus = new JLabel("LIVE DATA  ·  INR");
        lblSystemStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSystemStatus.setForeground(new Color(22, 101, 52));
        lblSystemStatus.setOpaque(true);
        lblSystemStatus.setBackground(new Color(220, 252, 231));
        lblSystemStatus.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        rightBadge.add(lblSystemStatus);

        bar.add(titleBox, BorderLayout.WEST);
        bar.add(rightBadge, BorderLayout.EAST);
        return bar;
    }

    private JPanel createStatCardsPanel() {
        DashboardStats stats = dashboardService.getDashboardStats();

        // Responsive grid with 5 cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 5, 14, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // 1. Total Consumers
        lblTotalConsumersVal = new JLabel(stats.formattedConsumers());
        cardsGrid.add(createCard(
                "Total Consumers",
                lblTotalConsumersVal,
                UIIcons.Kind.CONSUMERS,
                "Active Metered Accounts",
                new Color(37, 99, 235),  // Electric Blue
                new Color(239, 246, 255)
        ));

        // 2. Total Bills
        lblTotalBillsVal = new JLabel(stats.formattedBills());
        cardsGrid.add(createCard(
                "Total Bills",
                lblTotalBillsVal,
                UIIcons.Kind.BILL,
                "Cycle Invoices Issued",
                new Color(2, 132, 199),  // Sky Blue
                new Color(240, 249, 255)
        ));

        // 3. Paid Bills
        lblPaidBillsVal = new JLabel(stats.formattedPaidBills());
        cardsGrid.add(createCard(
                "Paid Bills",
                lblPaidBillsVal,
                UIIcons.Kind.PAYMENT,
                String.format("%.1f%% Collection Rate", stats.paidPercentage()),
                new Color(22, 163, 74),  // Green
                new Color(240, 253, 244)
        ));

        // 4. Unpaid Bills
        lblUnpaidBillsVal = new JLabel(stats.formattedUnpaidBills());
        cardsGrid.add(createCard(
                "Unpaid Bills",
                lblUnpaidBillsVal,
                UIIcons.Kind.HISTORY,
                "Overdue & Pending",
                new Color(234, 88, 12),  // Amber/Orange
                new Color(255, 247, 237)
        ));

        // 5. Total Revenue
        lblTotalRevenueVal = new JLabel(stats.formattedRevenue());
        cardsGrid.add(createCard(
                "Total Revenue",
                lblTotalRevenueVal,
                UIIcons.Kind.REPORTS,
                "Total Settled Funds",
                new Color(124, 58, 237), // Purple/Indigo
                new Color(250, 245, 255)
        ));

        return cardsGrid;
    }

    private JPanel createCard(String title, JLabel valueLabel, UIIcons.Kind icon, String footer, Color accentColor, Color bgBadge) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw top accent bar
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, getWidth(), 5, 4, 4);
                g2d.dispose();
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Top Header Row with Title and Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(UIConstants.TEXT_MUTED);

        JLabel lblIcon = new JLabel(UIIcons.of(icon));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(bgBadge);
        lblIcon.setForeground(accentColor);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setBorder(new EmptyBorder(5, 5, 5, 5));

        topRow.add(lblTitle, BorderLayout.WEST);
        topRow.add(lblIcon, BorderLayout.EAST);
        card.add(topRow);
        card.add(Box.createVerticalStrut(8));

        // Value Label
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(UIConstants.TEXT_DARK);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));

        // Footer Note
        JLabel lblFooter = new JLabel(footer);
        if ("Paid Bills".equals(title)) lblPaidRate = lblFooter;
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(UIConstants.TEXT_MUTED);
        lblFooter.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblFooter);

        return card;
    }

    private JPanel createRecentActivitySection() {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(UIConstants.CARD_BG);
        sectionPanel.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        // Section Title
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lblSectionTitle = new JLabel("Recent activity");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSectionTitle.setForeground(UIConstants.TEXT_DARK);

        JLabel lblSub = new JLabel("Audit feed of recent transactions, readings, and updates");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(UIConstants.TEXT_MUTED);

        titleRow.add(lblSectionTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);
        sectionPanel.add(titleRow);
        sectionPanel.add(Box.createVerticalStrut(12));

        // Activity Table
        String[] columns = {"Timestamp", "Category", "Activity Title", "Details / Description", "Status"};
        List<RecentActivity> activities = dashboardService.getRecentActivities();

        Object[][] data = new Object[activities.size()][5];
        for (int i = 0; i < activities.size(); i++) {
            RecentActivity act = activities.get(i);
            data[i][0] = act.timestamp();
            data[i][1] = act.activityType();
            data[i][2] = act.title();
            data[i][3] = act.description();
            data[i][4] = act.status();
        }

        activityTableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(activityTableModel);
        UIConstants.styleTable(table);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(360);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);

        // Status pill renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String status = String.valueOf(val);
                if ("Completed".equalsIgnoreCase(status) || "Verified".equalsIgnoreCase(status)) {
                    lbl.setForeground(new Color(22, 101, 52));
                } else {
                    lbl.setForeground(new Color(194, 65, 12));
                }
                return lbl;
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1));
        tableScroll.setPreferredSize(new Dimension(0, 220));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        sectionPanel.add(tableScroll);

        return sectionPanel;
    }

    private JPanel createOperationalNoticeBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(241, 245, 249));
        banner.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lblNotice = new JLabel("Statistics and recent activity reflect the latest data available in the connected system.");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNotice.setForeground(UIConstants.TEXT_MUTED);

        banner.add(lblNotice, BorderLayout.CENTER);
        return banner;
    }

    /** Reload live KPIs and activity after administrator actions change database rows. */
    public void refreshData() {
        DashboardStats stats = dashboardService.getDashboardStats();
        lblTotalConsumersVal.setText(stats.formattedConsumers());
        lblTotalBillsVal.setText(stats.formattedBills());
        lblPaidBillsVal.setText(stats.formattedPaidBills());
        lblUnpaidBillsVal.setText(stats.formattedUnpaidBills());
        lblTotalRevenueVal.setText(stats.formattedRevenue());
        lblPaidRate.setText(String.format("%.1f%% Collection Rate", stats.paidPercentage()));

        activityTableModel.setRowCount(0);
        for (RecentActivity activity : dashboardService.getRecentActivities()) {
            activityTableModel.addRow(new Object[]{activity.timestamp(), activity.activityType(), activity.title(),
                    activity.description(), activity.status()});
        }
        revalidate();
        repaint();
    }
}
