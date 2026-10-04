package com.electricity.auth.ui;

import com.electricity.admindashboard.AdminDashboard;
import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.service.AdminDashboardService;
import com.electricity.admindashboard.service.JdbcAdminDashboardService;
import com.electricity.admindashboard.service.JdbcConsumerManagementService;
import com.electricity.admindashboard.service.JdbcBillHistoryService;
import com.electricity.admindashboard.service.JdbcPaymentManagementService;
import com.electricity.admindashboard.service.JdbcReportsService;
import com.electricity.auth.model.AuthResult;
import com.electricity.auth.model.UserRole;
import com.electricity.auth.service.AuthService;
import com.electricity.auth.service.JdbcAuthService;
import com.electricity.billgeneration.service.JdbcBillService;
import com.electricity.userdashboard.UserDashboard;
import com.electricity.userdashboard.service.JdbcPaymentService;
import com.electricity.userdashboard.service.JdbcUserDashboardService;
import com.electricity.userdashboard.service.UserDashboardService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Modern desktop Login Frame for the Electricity Billing System.
 * Built using Java 21 Swing with clean separation from authentication logic.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final AdminDashboardService adminDashboardService;
    private final UserDashboardService userDashboardService;

    // UI Components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JRadioButton rbUserRole;
    private JRadioButton rbAdminRole;
    private ButtonGroup roleGroup;
    private JCheckBox chkShowPassword;
    private JLabel lblStatusMessage;
    private JButton btnLogin;
    private JButton btnClear;
    private JButton btnExit;

    public LoginFrame() {
        this(new JdbcAuthService(), new JdbcAdminDashboardService(), new JdbcUserDashboardService());
    }

    public LoginFrame(AuthService authService) {
        this(authService, new JdbcAdminDashboardService(), new JdbcUserDashboardService());
    }

    public LoginFrame(
            AuthService authService,
            AdminDashboardService adminDashboardService,
            UserDashboardService userDashboardService
    ) {
        this.authService = authService;
        this.adminDashboardService = adminDashboardService;
        this.userDashboardService = userDashboardService;
        initializeFrame();
        initComponents();
        setupEvents();
    }

    private void initializeFrame() {
        UIConstants.installDefaults();
        setTitle("Electricity Billing System - Login");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setSize(960, 660);
        setMinimumSize(new Dimension(930, 640));
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        getContentPane().setBackground(UIConstants.BG_LIGHT);
        setLayout(new BorderLayout());

        // Intercept window closing to show confirmation dialog
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExitAction();
            }
        });
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_LIGHT);
        root.add(createBrandPanel(), BorderLayout.WEST);
        root.add(createLoginForm(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.SIDEBAR_BG,
                        getWidth(), getHeight(), UIConstants.PRIMARY_NAVY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBorder(new EmptyBorder(38, 34, 30, 34));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        JPanel mark = new JPanel(new GridBagLayout());
        mark.setBackground(UIConstants.ACCENT_BLUE);
        mark.setPreferredSize(new Dimension(42, 42));
        JLabel bolt = new JLabel(UIIcons.of(UIIcons.Kind.LIGHTNING));
        bolt.setForeground(new Color(255, 232, 153));
        mark.add(bolt);
        JPanel brandCopy = new JPanel();
        brandCopy.setOpaque(false);
        brandCopy.setLayout(new BoxLayout(brandCopy, BoxLayout.Y_AXIS));
        JLabel brandName = new JLabel("ELECTRICITY BILLING");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brandName.setForeground(Color.WHITE);
        JLabel brandSub = new JLabel("SYSTEM PORTAL");
        brandSub.setFont(UIConstants.FONT_BADGE);
        brandSub.setForeground(new Color(174, 193, 216));
        brandCopy.add(brandName);
        brandCopy.add(Box.createVerticalStrut(4));
        brandCopy.add(brandSub);
        brand.add(mark);
        brand.add(brandCopy);

        JPanel hero = new JPanel();
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("POWERING BETTER SERVICE");
        eyebrow.setFont(UIConstants.FONT_BADGE);
        eyebrow.setForeground(new Color(147, 197, 253));
        JLabel headline = new JLabel("<html>Electricity billing,<br>made simple.</html>");
        headline.setFont(new Font("Segoe UI", Font.BOLD, 31));
        headline.setForeground(Color.WHITE);
        JLabel description = new JLabel("<html>Manage consumer accounts, meter readings,<br>invoices, and payments in one secure workspace.</html>");
        description.setFont(UIConstants.FONT_SUBTITLE);
        description.setForeground(new Color(203, 218, 238));
        hero.add(eyebrow);
        hero.add(Box.createVerticalStrut(16));
        hero.add(headline);
        hero.add(Box.createVerticalStrut(14));
        hero.add(description);

        JLabel accessNote = new JLabel("AUTHORIZED ACCESS ONLY");
        accessNote.setFont(UIConstants.FONT_BADGE);
        accessNote.setForeground(new Color(174, 193, 216));
        accessNote.setBorder(new CompoundBorder(
                new LineBorder(new Color(68, 91, 117), 1, true), new EmptyBorder(9, 11, 9, 11)));

        panel.add(brand, BorderLayout.NORTH);
        panel.add(hero, BorderLayout.CENTER);
        panel.add(accessNote, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createLoginForm() {
        JPanel surface = new JPanel(new GridBagLayout());
        surface.setBackground(UIConstants.BG_LIGHT);
        surface.setBorder(new EmptyBorder(26, 28, 26, 28));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(22, 34, 20, 34)));
        card.setPreferredSize(new Dimension(500, 540));

        JLabel eyebrow = new JLabel("ACCOUNT ACCESS");
        eyebrow.setFont(UIConstants.FONT_BADGE);
        eyebrow.setForeground(UIConstants.ACCENT_BLUE);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(eyebrow);
        card.add(Box.createVerticalStrut(4));

        JLabel title = new JLabel("Welcome back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(3));

        JLabel subtitle = new JLabel("Sign in to continue to your workspace.");
        subtitle.setFont(UIConstants.FONT_SUBTITLE);
        subtitle.setForeground(UIConstants.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(14));

        JLabel roleLabel = formLabel("SIGN IN AS");
        card.add(roleLabel);
        card.add(Box.createVerticalStrut(5));
        JPanel roles = createRoleSelector();
        roles.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roles);
        card.add(Box.createVerticalStrut(12));

        card.add(formLabel("Username"));
        card.add(Box.createVerticalStrut(7));
        txtUsername = new JTextField();
        styleTextInput(txtUsername);
        setFormWidth(txtUsername, 420, 42);
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(10));

        card.add(formLabel("Password"));
        card.add(Box.createVerticalStrut(7));
        txtPassword = new JPasswordField();
        styleTextInput(txtPassword);
        setFormWidth(txtPassword, 420, 42);
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(4));

        chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setFont(UIConstants.FONT_MESSAGE);
        chkShowPassword.setForeground(UIConstants.TEXT_MUTED);
        chkShowPassword.setBackground(Color.WHITE);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(chkShowPassword);
        card.add(Box.createVerticalStrut(4));

        lblStatusMessage = new JLabel(" ");
        lblStatusMessage.setFont(UIConstants.FONT_MESSAGE);
        lblStatusMessage.setForeground(UIConstants.ERROR_RED);
        lblStatusMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        setFormWidth(lblStatusMessage, 420, 22);
        card.add(lblStatusMessage);
        card.add(Box.createVerticalStrut(5));

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setPreferredSize(new Dimension(420, 46));
        actions.setMaximumSize(new Dimension(420, 46));
        btnLogin = createStyledButton("Sign in", UIConstants.ACCENT_BLUE, UIConstants.HOVER_BLUE, Color.WHITE);
        btnClear = new JButton("Clear fields");
        UIConstants.styleButton(btnClear, Color.WHITE, UIConstants.PRIMARY_NAVY);
        actions.add(btnLogin);
        actions.add(btnClear);
        card.add(actions);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        setFormWidth(bottomRow, 420, 30);
        JLabel accountHint = new JLabel("Use the account provided by your administrator.");
        accountHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        accountHint.setForeground(UIConstants.TEXT_MUTED);
        btnExit = new JButton("Exit");
        btnExit.setFont(UIConstants.FONT_MESSAGE);
        btnExit.setForeground(UIConstants.TEXT_MUTED);
        btnExit.setBorder(new EmptyBorder(4, 6, 4, 0));
        btnExit.setContentAreaFilled(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bottomRow.add(accountHint, BorderLayout.WEST);
        bottomRow.add(btnExit, BorderLayout.EAST);
        card.add(Box.createVerticalStrut(10));
        card.add(bottomRow);

        surface.add(card);
        return surface;
    }

    private JPanel createRoleSelector() {
        JPanel roles = new JPanel(new GridLayout(1, 2, 10, 0));
        roles.setOpaque(false);
        roles.setMaximumSize(new Dimension(430, 58));
        rbUserRole = new JRadioButton("Consumer", true);
        rbAdminRole = new JRadioButton("Administrator", false);
        roleGroup = new ButtonGroup();
        roleGroup.add(rbUserRole);
        roleGroup.add(rbAdminRole);
        styleRoleOption(rbUserRole);
        styleRoleOption(rbAdminRole);
        rbUserRole.addActionListener(event -> refreshRoleOptions());
        rbAdminRole.addActionListener(event -> refreshRoleOptions());
        refreshRoleOptions();
        roles.setPreferredSize(new Dimension(420, 58));
        roles.add(rbUserRole);
        roles.add(rbAdminRole);
        return roles;
    }

    private static void styleRoleOption(JRadioButton option) {
        option.setFont(UIConstants.FONT_LABEL);
        option.setForeground(UIConstants.TEXT_DARK);
        option.setBackground(Color.WHITE);
        option.setOpaque(true);
        option.setFocusPainted(false);
        option.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        option.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 8)));
    }

    private void refreshRoleOptions() {
        rbUserRole.setBackground(rbUserRole.isSelected() ? new Color(239, 246, 255) : Color.WHITE);
        rbAdminRole.setBackground(rbAdminRole.isSelected() ? new Color(239, 246, 255) : Color.WHITE);
        rbUserRole.setBorder(new CompoundBorder(new LineBorder(
                rbUserRole.isSelected() ? UIConstants.ACCENT_BLUE : UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 8)));
        rbAdminRole.setBorder(new CompoundBorder(new LineBorder(
                rbAdminRole.isSelected() ? UIConstants.ACCENT_BLUE : UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 8)));
    }

    private static JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_LABEL);
        label.setForeground(UIConstants.TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static void setFormWidth(JComponent component, int width, int height) {
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleTextInput(JTextField field) {
        field.setFont(UIConstants.FONT_INPUT);
        field.setForeground(UIConstants.TEXT_DARK);
        field.setBackground(Color.WHITE);
        field.setCaretColor(UIConstants.ACCENT_BLUE);
        field.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private JButton createStyledButton(String text, Color baseColor, Color hoverColor, Color textColor) {
        JButton btn = new JButton(text);
        UIConstants.styleButton(btn, baseColor, textColor);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
            }
        });

        return btn;
    }

    private void setupEvents() {
        // Login button action
        btnLogin.addActionListener(e -> performLogin());

        // Clear button action
        btnClear.addActionListener(e -> clearFields());

        // Exit button action
        btnExit.addActionListener(e -> handleExitAction());

        // Toggle password masking
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });

        // Enter key listeners on inputs to trigger login
        KeyAdapter enterSubmitListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };

        txtUsername.addKeyListener(enterSubmitListener);
        txtPassword.addKeyListener(enterSubmitListener);
    }

    /**
     * Executes input validation and delegates authentication to the AuthService.
     */
    public void performLogin() {
        String username = txtUsername.getText().trim();
        char[] password = txtPassword.getPassword();
        UserRole role = rbAdminRole.isSelected() ? UserRole.ADMIN : UserRole.USER;

        // Reset status message
        lblStatusMessage.setText(" ");

        // Client-side quick validation
        if (username.isEmpty()) {
            showValidationError("Please enter your username.", txtUsername);
            return;
        }

        if (password.length == 0) {
            showValidationError("Please enter your password.", txtPassword);
            return;
        }

        btnLogin.setEnabled(false);
        lblStatusMessage.setForeground(UIConstants.TEXT_MUTED);
        lblStatusMessage.setText("Checking account...");

        new SwingWorker<AuthResult, Void>() {
            @Override
            protected AuthResult doInBackground() {
                try {
                    return authService.authenticate(username, password, role);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                txtPassword.setText("");
                try {
                    showLoginResult(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showLoginFailure("Login was interrupted. Please try again.");
                } catch (ExecutionException exception) {
                    showLoginFailure("Login could not be completed. Check the MySQL connection and try again.");
                }
            }
        }.execute();
    }

    private void showLoginResult(AuthResult result) {
        if (!result.success()) {
            showLoginFailure(result.message());
            return;
        }

        lblStatusMessage.setForeground(UIConstants.SUCCESS_GREEN);
        lblStatusMessage.setText("Authentication successful!");
        JOptionPane.showMessageDialog(
                this,
                result.message(),
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE
        );
        onLoginSuccess(result);
    }

    private void showLoginFailure(String message) {
        lblStatusMessage.setForeground(UIConstants.ERROR_RED);
        lblStatusMessage.setText(message);
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.ERROR_MESSAGE);
        txtPassword.requestFocusInWindow();
    }

    /**
     * Hook called after successful login.
     * Launches the Admin Dashboard when an Administrator signs in.
     */
    protected void onLoginSuccess(AuthResult result) {
        try {
            Runnable logoutCallback = () -> {
                this.clearFields();
                this.setVisible(true);
            };
            if (result.role() == UserRole.ADMIN) {
                AdminProfile profile = adminDashboardService.getAdminProfile(result.username());
                AdminDashboard adminDashboard = new AdminDashboard(
                        profile,
                        adminDashboardService,
                        new JdbcConsumerManagementService(result.username()),
                        new JdbcBillService(result.username()),
                        new JdbcBillHistoryService(),
                        new JdbcPaymentManagementService(result.username()),
                        new JdbcReportsService(),
                        logoutCallback
                );
                adminDashboard.setVisible(true);
                this.setVisible(false);
            } else if (result.role() == UserRole.USER) {
                UserDashboard userDashboard = new UserDashboard(
                        userDashboardService, new JdbcPaymentService(), result.username(), logoutCallback
                );
                userDashboard.setVisible(true);
                this.setVisible(false);
            }
        } catch (RuntimeException exception) {
            String message = exception.getMessage() == null
                    ? "The dashboard could not load its account data."
                    : exception.getMessage();
            lblStatusMessage.setForeground(UIConstants.ERROR_RED);
            lblStatusMessage.setText("Dashboard could not load.");
            JOptionPane.showMessageDialog(this, message, "Dashboard Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showValidationError(String message, JComponent targetComponent) {
        lblStatusMessage.setForeground(UIConstants.ERROR_RED);
        lblStatusMessage.setText(message);
        targetComponent.requestFocusInWindow();
    }

    /**
     * Resets all fields to their default states.
     */
    public void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        rbUserRole.setSelected(true);
        chkShowPassword.setSelected(false);
        txtPassword.setEchoChar('•');
        lblStatusMessage.setText(" ");
        txtUsername.requestFocusInWindow();
    }

    /**
     * Displays a confirmation dialog before safely shutting down the application.
     */
    private void handleExitAction() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the Electricity Billing System?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Generates a sleek lightning bolt icon for the window title bar and taskbar.
     */
    private Image createAppIcon() {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Circular background
        g2.setColor(UIConstants.PRIMARY_NAVY);
        g2.fillOval(2, 2, size - 4, size - 4);

        // Yellow electric bolt polygon
        g2.setColor(new Color(250, 204, 21)); // Vibrant gold
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

    // Getters for programmatic testing and future module integration
    public JTextField getTxtUsername() {
        return txtUsername;
    }

    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    public JRadioButton getRbUserRole() {
        return rbUserRole;
    }

    public JRadioButton getRbAdminRole() {
        return rbAdminRole;
    }

    public JLabel getLblStatusMessage() {
        return lblStatusMessage;
    }
}
