package com.electricity.admindashboard;

import com.electricity.billgeneration.BillGenerationPanel;
import com.electricity.billgeneration.service.BillService;
import com.electricity.billgeneration.service.MockBillService;
import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.service.AdminDashboardService;
import com.electricity.admindashboard.service.ConsumerManagementService;
import com.electricity.admindashboard.service.BillHistoryService;
import com.electricity.admindashboard.service.PaymentManagementService;
import com.electricity.admindashboard.service.ReportsService;
import com.electricity.admindashboard.service.MockConsumerManagementService;
import com.electricity.admindashboard.service.MockAdminDashboardService;
import com.electricity.admindashboard.service.MockBillHistoryService;
import com.electricity.admindashboard.service.MockPaymentManagementService;
import com.electricity.admindashboard.service.MockReportsService;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Main window for the Admin Dashboard in the Electricity Billing System.
 * Houses the top header bar, left sidebar navigation, and card-swapped main content views.
 */
public class AdminDashboard extends JFrame implements SidebarPanel.NavigationListener {

    private final AdminDashboardService dashboardService;
    private final ConsumerManagementService consumerManagementService;
    private final BillService billService;
    private final BillHistoryService billHistoryService;
    private final PaymentManagementService paymentManagementService;
    private final ReportsService reportsService;
    private final AdminProfile currentAdmin;
    private final Runnable logoutCallback;

    // Center Card Navigation
    private CardLayout cardLayout;
    private JPanel mainContentArea;

    // View Panels
    private DashboardPanel dashboardPanel;
    private ProfilePanel profilePanel;
    private ConsumerManagementPanel consumerManagementPanel;
    private AdminBillHistoryPanel billHistoryPanel;
    private AdminPaymentPanel paymentPanel;
    private ReportsPanel reportsPanel;

    public AdminDashboard() {
        this(new MockAdminDashboardService().getAdminProfile("admin"), new MockAdminDashboardService(), null);
    }

    public AdminDashboard(AdminProfile adminProfile) {
        this(adminProfile, new MockAdminDashboardService(), null);
    }

    public AdminDashboard(AdminProfile adminProfile, Runnable logoutCallback) {
        this(adminProfile, new MockAdminDashboardService(), logoutCallback);
    }

    public AdminDashboard(
            AdminProfile adminProfile,
            AdminDashboardService dashboardService,
            Runnable logoutCallback
    ) {
        this(adminProfile, dashboardService, new MockConsumerManagementService(), new MockBillService(),
                new MockBillHistoryService(), new MockPaymentManagementService(), new MockReportsService(), logoutCallback);
    }

    public AdminDashboard(
            AdminProfile adminProfile,
            AdminDashboardService dashboardService,
            ConsumerManagementService consumerManagementService,
            BillService billService,
            Runnable logoutCallback
    ) {
        this(adminProfile, dashboardService, consumerManagementService, billService,
                new MockBillHistoryService(), new MockPaymentManagementService(), new MockReportsService(), logoutCallback);
    }

    public AdminDashboard(
            AdminProfile adminProfile,
            AdminDashboardService dashboardService,
            ConsumerManagementService consumerManagementService,
            BillService billService,
            BillHistoryService billHistoryService,
            PaymentManagementService paymentManagementService,
            ReportsService reportsService,
            Runnable logoutCallback
    ) {
        this.dashboardService = dashboardService;
        this.consumerManagementService = consumerManagementService;
        this.billService = billService;
        this.billHistoryService = billHistoryService;
        this.paymentManagementService = paymentManagementService;
        this.reportsService = reportsService;
        this.currentAdmin = adminProfile != null ? adminProfile : dashboardService.getAdminProfile("admin");
        this.logoutCallback = logoutCallback;

        initializeWindow();
        initComponents();
    }

    private void initializeWindow() {
        UIConstants.installDefaults();
        setTitle("Electricity Billing System — Admin Dashboard Console");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1040, 640));
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        getContentPane().setBackground(UIConstants.BG_LIGHT);
        setLayout(new BorderLayout());

        // Intercept window close to trigger logout confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndLogout();
            }
        });
    }

    private void initComponents() {
        // 1. Top Header Bar
        add(createTopHeader(), BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        SidebarPanel sidebar = new SidebarPanel(this);
        add(sidebar, BorderLayout.WEST);

        // 3. Center Content Area with CardLayout
        cardLayout = new CardLayout();
        mainContentArea = new JPanel(cardLayout);
        mainContentArea.setBackground(UIConstants.BG_LIGHT);

        // Assemble Content Views
        dashboardPanel = new DashboardPanel(dashboardService, currentAdmin);
        profilePanel   = new ProfilePanel(dashboardService, currentAdmin);

        // Consumer Management matches the supplied reference screen.
        consumerManagementPanel = new ConsumerManagementPanel(consumerManagementService);
        // Keep Bill Generation as a fully functional existing module.
        BillGenerationPanel billGenPanel = new BillGenerationPanel(billService);

        billHistoryPanel = new AdminBillHistoryPanel(billHistoryService);
        paymentPanel = new AdminPaymentPanel(paymentManagementService);
        reportsPanel = new ReportsPanel(reportsService);

        // Meter Reading shares the validated reading flow with bill generation,
        // which saves both records atomically to prevent orphan readings.
        mainContentArea.add(dashboardPanel,          SidebarPanel.VIEW_DASHBOARD);
        mainContentArea.add(consumerManagementPanel, SidebarPanel.VIEW_CONSUMER_MANAGEMENT);
        mainContentArea.add(billGenPanel,             SidebarPanel.VIEW_BILL_GENERATION);
        mainContentArea.add(billHistoryPanel,         SidebarPanel.VIEW_BILL_HISTORY);
        mainContentArea.add(paymentPanel,             SidebarPanel.VIEW_PAYMENTS);
        mainContentArea.add(reportsPanel,             SidebarPanel.VIEW_REPORTS);
        mainContentArea.add(profilePanel,             SidebarPanel.VIEW_PROFILE);

        add(mainContentArea, BorderLayout.CENTER);
        // Start on the live KPI and activity view after a database-backed login.
        cardLayout.show(mainContentArea, SidebarPanel.VIEW_DASHBOARD);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                        0, 0, UIConstants.PRIMARY_NAVY,
                        getWidth(), 0, new Color(30, 64, 175)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };

        header.setPreferredSize(new Dimension(0, 62));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(0, 20, 0, 24));

        // Left title & system status
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        leftBox.setOpaque(false);

        JLabel lblTitle = new JLabel("ELECTRICITY BILLING SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSep = new JLabel("|");
        lblSep.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSep.setForeground(new Color(147, 197, 253));

        JLabel lblSub = new JLabel("Administrator workspace");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(219, 234, 254));

        leftBox.add(lblTitle);
        leftBox.add(lblSep);
        leftBox.add(lblSub);

        // Right user profile summary badge
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        rightBox.setOpaque(false);

        JLabel avatar = new JLabel(initials(currentAdmin.fullName()), SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(55, 99, 159));
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setBorder(new LineBorder(new Color(112, 151, 204), 1, true));

        JPanel userCopy = new JPanel();
        userCopy.setOpaque(false);
        userCopy.setLayout(new BoxLayout(userCopy, BoxLayout.Y_AXIS));
        JLabel lblUserName = new JLabel(currentAdmin.fullName());
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserName.setForeground(Color.WHITE);
        JLabel lblUserRole = new JLabel("Administrator");
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblUserRole.setForeground(new Color(203, 218, 238));
        userCopy.add(lblUserName);
        userCopy.add(lblUserRole);
        rightBox.add(avatar);
        rightBox.add(userCopy);

        header.add(leftBox, BorderLayout.WEST);
        header.add(rightBox, BorderLayout.EAST);

        return header;
    }

    private static String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "AD";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1
                ? (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase()
                : parts[0].substring(0, 1).toUpperCase();
    }

    @Override
    public void onNavigate(String viewKey) {
        if (cardLayout != null && mainContentArea != null) {
            try {
                if (SidebarPanel.VIEW_DASHBOARD.equals(viewKey)) dashboardPanel.refreshData();
                if (SidebarPanel.VIEW_BILL_HISTORY.equals(viewKey)) billHistoryPanel.refreshData();
                if (SidebarPanel.VIEW_PAYMENTS.equals(viewKey)) paymentPanel.refreshData();
                if (SidebarPanel.VIEW_REPORTS.equals(viewKey)) reportsPanel.refreshData();
            } catch (RuntimeException exception) {
                String message = exception.getMessage() == null ? "This module could not refresh its database data." : exception.getMessage();
                JOptionPane.showMessageDialog(this, message, "Data Refresh Failed", JOptionPane.ERROR_MESSAGE);
            }
            // Keep the old route usable for callers; the sidebar now exposes only Bill generation.
            cardLayout.show(mainContentArea,
                    SidebarPanel.VIEW_METER_READING.equals(viewKey) ? SidebarPanel.VIEW_BILL_GENERATION : viewKey);
        }
    }

    @Override
    public void onLogout() {
        confirmAndLogout();
    }

    private void confirmAndLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            if (logoutCallback != null) {
                logoutCallback.run();
            }
        }
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

    /**
     * Standalone main method allowing developers to run and test the Admin Dashboard directly.
     */
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIConstants.installDefaults();

        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }

    // Accessors for UI unit testing
    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public ProfilePanel getProfilePanel() {
        return profilePanel;
    }

    public ConsumerManagementPanel getConsumerManagementPanel() {
        return consumerManagementPanel;
    }

    public AdminBillHistoryPanel getBillHistoryPanel() { return billHistoryPanel; }
    public AdminPaymentPanel getPaymentPanel() { return paymentPanel; }
    public ReportsPanel getReportsPanel() { return reportsPanel; }
}
