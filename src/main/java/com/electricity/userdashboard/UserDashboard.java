package com.electricity.userdashboard;

import com.electricity.auth.ui.UIConstants;
import com.electricity.userdashboard.model.ConsumerBill;
import com.electricity.userdashboard.model.ConsumerProfile;
import com.electricity.userdashboard.service.MockUserDashboardService;
import com.electricity.userdashboard.service.MockPaymentService;
import com.electricity.userdashboard.service.PaymentService;
import com.electricity.userdashboard.service.UserDashboardService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Main consumer window with CardLayout navigation for dashboard, bills, payments, and profile. */
public class UserDashboard extends JFrame implements UserSidebarPanel.NavigationListener {
    private final UserDashboardService dashboardService;
    private final PaymentService paymentService;
    private final ConsumerProfile currentConsumer;
    private final ConsumerBill currentBill;
    private final Runnable logoutCallback;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    public UserDashboard() {
        this(new MockUserDashboardService(), new MockPaymentService(), "user", null);
    }

    public UserDashboard(String username, Runnable logoutCallback) {
        this(new MockUserDashboardService(), new MockPaymentService(), username, logoutCallback);
    }

    public UserDashboard(UserDashboardService service, String username, Runnable logoutCallback) {
        this(service, new MockPaymentService(), username, logoutCallback);
    }

    public UserDashboard(
            UserDashboardService service,
            PaymentService paymentService,
            String username,
            Runnable logoutCallback
    ) {
        dashboardService = service;
        this.paymentService = paymentService;
        currentConsumer = service.getConsumerProfile(username);
        currentBill = service.getCurrentBill(currentConsumer.consumerNumber());
        this.logoutCallback = logoutCallback;
        initializeWindow();
        initializeComponents();
    }

    private void initializeWindow() {
        UIConstants.installDefaults();
        setTitle("Electricity Billing System - User Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        getContentPane().setBackground(UIConstants.BG_LIGHT);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmAndLogout(); }
        });
    }

    private void initializeComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(new UserSidebarPanel(this), BorderLayout.WEST);
        content.setBackground(UIConstants.BG_LIGHT);
        content.add(createHomePanel(), UserSidebarPanel.VIEW_DASHBOARD);
        content.add(createBillPanel(), UserSidebarPanel.VIEW_BILL);
        content.add(createHistoryPanel(), UserSidebarPanel.VIEW_HISTORY);
        content.add(createPaymentPanel(), UserSidebarPanel.VIEW_PAYMENT);
        content.add(createProfilePanel(), UserSidebarPanel.VIEW_PROFILE);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY_NAVY);
        header.setBorder(new EmptyBorder(0, 22, 0, 26));
        header.setPreferredSize(new Dimension(0, 66));
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("ELECTRICITY BILLING SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Consumer portal");
        subtitle.setFont(UIConstants.FONT_MESSAGE);
        subtitle.setForeground(new Color(203, 218, 238));
        brand.add(title);
        brand.add(Box.createVerticalStrut(3));
        brand.add(subtitle);

        JPanel user = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        user.setOpaque(false);
        JLabel avatar = new JLabel(initials(currentConsumer.consumerName()), SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(55, 99, 159));
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBorder(new LineBorder(new Color(112, 151, 204), 1, true));
        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(currentConsumer.consumerName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(Color.WHITE);
        JLabel number = new JLabel("Consumer ID  " + currentConsumer.consumerNumber());
        number.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        number.setForeground(new Color(203, 218, 238));
        identity.add(name);
        identity.add(Box.createVerticalStrut(2));
        identity.add(number);
        user.add(avatar);
        user.add(identity);
        header.add(brand, BorderLayout.WEST);
        header.add(user, BorderLayout.EAST);
        return header;
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "CU";
        String[] parts = name.trim().split("\\s+");
        return parts.length > 1
                ? (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT)
                : parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private JPanel createHomePanel() {
        JPanel panel = pagePanel("Dashboard", "Welcome back, " + currentConsumer.consumerName() + "");
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);
        grid.add(infoCard("Consumer", currentConsumer.consumerName(), "Consumer number: " + currentConsumer.consumerNumber()));
        grid.add(infoCard("Units used", formatDecimal(currentBill.unitsConsumed()),
                "Meter " + currentConsumer.meterNumber() + " · Period " + currentBill.billingPeriod()));
        grid.add(infoCard("Current bill", money(currentBill.totalAmount()), "Due date: " + currentBill.dueDate()));
        grid.add(infoCard("Payment status", displayPaymentStatus(currentBill.paymentStatus()),
                "Amount due: " + money(currentBill.outstandingAmount())));
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBillPanel() {
        JPanel panel = pagePanel("My Bill", "Current electricity bill");
        JPanel details = new JPanel(new GridLayout(0, 2, 0, 0));
        details.setBackground(Color.WHITE);
        details.setBorder(new LineBorder(UIConstants.BORDER_LIGHT));
        addDetail(details, "Bill number", currentBill.billNumber());
        addDetail(details, "Billing period", currentBill.billingPeriod());
        addDetail(details, "Previous meter reading", formatDecimal(currentBill.previousMeterReading()));
        addDetail(details, "Current meter reading", formatDecimal(currentBill.currentMeterReading()));
        addDetail(details, "Units consumed", formatDecimal(currentBill.unitsConsumed()));
        addDetail(details, "Energy charge", money(currentBill.energyCharge()));
        addDetail(details, "Fixed charge", money(currentBill.fixedCharge()));
        addDetail(details, "Tax", money(currentBill.tax()));
        addDetail(details, "Total amount", money(currentBill.totalAmount()));
        addDetail(details, "Payment status", currentBill.paymentStatus());
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = pagePanel("Bill History", "Previous billing records");
        String[] columns = {"Bill Number", "Billing Period", "Units", "Amount", "Payment Status", "Due Date"};
        Object[][] rows = dashboardService.getBillHistory(currentConsumer.consumerNumber()).stream()
                .map(bill -> new Object[]{bill.billNumber(), bill.billingPeriod(), formatDecimal(bill.unitsConsumed()), money(bill.totalAmount()), bill.paymentStatus(), bill.dueDate()})
                .toArray(Object[][]::new);
        JTable table = new JTable(rows, columns);
        UIConstants.styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = pagePanel("Payment Status", "Record a request for your bill balance");
        JPanel card = infoCard("Amount due", money(currentBill.outstandingAmount()), "Due date: " + currentBill.dueDate());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        actions.setOpaque(false);
        JButton recordPayment = new JButton("Record Payment Request");
        UIConstants.styleButton(recordPayment, UIConstants.ACCENT_BLUE, Color.WHITE);
        String paymentStatus = currentBill.paymentStatus();
        boolean alreadyPending = "PAYMENT_PENDING".equalsIgnoreCase(paymentStatus);
        boolean alreadyPaid = "PAID".equalsIgnoreCase(paymentStatus);
        boolean noBill = "No bill".equalsIgnoreCase(paymentStatus);
        recordPayment.setEnabled(!alreadyPending && !alreadyPaid && !noBill);
        JLabel status = new JLabel("Status: " + displayPaymentStatus(paymentStatus));
        status.setFont(UIConstants.FONT_LABEL);
        status.setForeground(alreadyPaid ? new Color(22, 101, 52) : new Color(194, 65, 12));
        recordPayment.addActionListener(e -> {
            JComboBox<String> method = new JComboBox<>(new String[]{"UPI", "CARD", "BANK_TRANSFER", "CASH"});
            JPanel requestForm = new JPanel(new BorderLayout(0, 10));
            requestForm.add(new JLabel("Payment method:"), BorderLayout.NORTH);
            requestForm.add(method, BorderLayout.CENTER);
            requestForm.add(new JLabel("This records a pending request only. No money is transferred."), BorderLayout.SOUTH);
            int choice = JOptionPane.showConfirmDialog(this, requestForm, "Record Payment Request",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return;
            try {
                String reference = paymentService.recordPendingPayment(
                        currentConsumer.consumerNumber(), currentBill.billNumber(), String.valueOf(method.getSelectedItem()));
                status.setText("Status: Payment request pending");
                status.setForeground(new Color(30, 64, 175));
                recordPayment.setEnabled(false);
                JOptionPane.showMessageDialog(this,
                        "The pending request was saved. Reference: " + reference,
                        "Payment Request Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (RuntimeException exception) {
                String message = exception.getMessage() == null ? "The request could not be saved." : exception.getMessage();
                JOptionPane.showMessageDialog(this, message, "Payment Request Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(recordPayment);
        actions.add(status);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        wrapper.add(actions, BorderLayout.CENTER);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private String displayPaymentStatus(String status) {
        if ("PAYMENT_PENDING".equalsIgnoreCase(status)) return "Payment request pending";
        if ("UNPAID".equalsIgnoreCase(status)) return "Unpaid";
        if ("PARTIALLY_PAID".equalsIgnoreCase(status)) return "Partially paid";
        if ("OVERDUE".equalsIgnoreCase(status)) return "Overdue";
        if ("PAID".equalsIgnoreCase(status)) return "Paid";
        return status;
    }

    private JPanel createProfilePanel() {
        JPanel panel = pagePanel("Profile", "Review and update your contact information");
        JPanel details = new JPanel(new GridLayout(0, 2, 0, 0));
        details.setBackground(Color.WHITE);
        details.setBorder(new LineBorder(UIConstants.BORDER_LIGHT));
        addDetail(details, "Consumer name", currentConsumer.consumerName());
        addDetail(details, "Consumer number", currentConsumer.consumerNumber());
        JTextField address = new JTextField("—".equals(currentConsumer.address()) ? "" : currentConsumer.address());
        JTextField phone = new JTextField(currentConsumer.phone());
        JTextField email = new JTextField(currentConsumer.email());
        UIConstants.styleTextField(address);
        UIConstants.styleTextField(phone);
        UIConstants.styleTextField(email);
        addEditableDetail(details, "Address", address);
        addDetail(details, "Meter number", currentConsumer.meterNumber());
        addDetail(details, "Tariff category", currentConsumer.tariffCategory());
        addEditableDetail(details, "Phone", phone);
        addEditableDetail(details, "Email", email);
        JButton save = new JButton("Save Contact Details");
        UIConstants.styleButton(save, UIConstants.ACCENT_BLUE, Color.WHITE);
        JLabel status = new JLabel(" ");
        status.setFont(UIConstants.FONT_MESSAGE);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        actions.setOpaque(false);
        actions.add(save);
        actions.add(status);
        save.addActionListener(event -> {
            try {
                boolean updated = dashboardService.updateContactDetails(currentConsumer.username(),
                        address.getText(), phone.getText(), email.getText());
                if (!updated) throw new IllegalStateException("The consumer profile could not be updated.");
                status.setText("Profile updated successfully.");
                status.setForeground(new Color(22, 101, 52));
            } catch (RuntimeException exception) {
                status.setText(exception.getMessage() == null ? "Profile update failed." : exception.getMessage());
                status.setForeground(new Color(185, 28, 28));
            }
        });
        panel.add(details, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel pagePanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(UIConstants.BG_LIGHT);
        panel.setBorder(new EmptyBorder(28, 30, 30, 30));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_PAGE_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_DARK);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UIConstants.FONT_SUBTITLE);
        subtitleLabel.setForeground(UIConstants.TEXT_MUTED);
        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitleLabel);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    private JPanel infoCard(String title, String value, String detail) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new javax.swing.border.CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT), new EmptyBorder(18, 18, 18, 18)));
        JLabel titleLabel = new JLabel(title.toUpperCase(Locale.ROOT));
        titleLabel.setFont(UIConstants.FONT_BADGE);
        titleLabel.setForeground(UIConstants.TEXT_MUTED);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        valueLabel.setForeground(UIConstants.PRIMARY_NAVY);
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(UIConstants.FONT_MESSAGE);
        detailLabel.setForeground(UIConstants.TEXT_MUTED);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(detailLabel);
        return card;
    }

    private void addDetail(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UIConstants.FONT_LABEL);
        labelComponent.setForeground(UIConstants.TEXT_MUTED);
        labelComponent.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(UIConstants.FONT_INPUT);
        valueComponent.setForeground(UIConstants.TEXT_DARK);
        valueComponent.setBorder(new EmptyBorder(12, 14, 12, 14));
        panel.add(labelComponent);
        panel.add(valueComponent);
    }

    private void addEditableDetail(JPanel panel, String label, JTextField field) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UIConstants.FONT_LABEL);
        labelComponent.setForeground(UIConstants.TEXT_MUTED);
        labelComponent.setBorder(new EmptyBorder(6, 14, 6, 14));
        field.setFont(UIConstants.FONT_INPUT);
        field.setBorder(new EmptyBorder(6, 8, 6, 8));
        panel.add(labelComponent);
        panel.add(field);
    }

    private String money(BigDecimal amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "Rs." + format.format(amount);
    }

    private String formatDecimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    @Override public void onNavigate(String viewKey) { cardLayout.show(content, viewKey); }

    @Override public void onLogout() { confirmAndLogout(); }

    private void confirmAndLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            if (logoutCallback != null) logoutCallback.run();
        }
    }

    private Image createAppIcon() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(UIConstants.PRIMARY_NAVY);
        graphics.fillOval(2, 2, 28, 28);
        graphics.setColor(new Color(250, 204, 21));
        Polygon bolt = new Polygon(new int[]{17, 11, 16, 14, 22, 17}, new int[]{6, 17, 17, 26, 14, 14}, 6);
        graphics.fillPolygon(bolt);
        graphics.dispose();
        return image;
    }

    public ConsumerProfile getCurrentConsumer() { return currentConsumer; }
    public ConsumerBill getCurrentBill() { return currentBill; }
    public JPanel getContent() { return content; }
}
