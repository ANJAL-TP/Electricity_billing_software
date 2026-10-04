package com.electricity.admindashboard;

import com.electricity.auth.ui.SidebarNavButton;
import com.electricity.auth.ui.UIConstants;
import com.electricity.auth.ui.UIIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Dark, keyboard-accessible navigation for the administrator workspace. */
public class SidebarPanel extends JPanel {
    public interface NavigationListener {
        void onNavigate(String viewKey);
        void onLogout();
    }

    public static final String VIEW_DASHBOARD           = "DASHBOARD";
    public static final String VIEW_CONSUMER_MANAGEMENT = "CONSUMER_MANAGEMENT";
    /** Legacy route retained for callers that open meter entry through billing. */
    public static final String VIEW_METER_READING       = "METER_READING";
    public static final String VIEW_BILL_GENERATION     = "BILL_GENERATION";
    public static final String VIEW_BILL_HISTORY        = "BILL_HISTORY";
    public static final String VIEW_PAYMENTS            = "PAYMENTS";
    public static final String VIEW_REPORTS             = "REPORTS";
    public static final String VIEW_PROFILE             = "PROFILE";

    private final NavigationListener navListener;
    private final List<SidebarNavButton> navButtons = new ArrayList<>();
    private SidebarNavButton selectedButton;

    public SidebarPanel(NavigationListener navListener) {
        this.navListener = navListener;
        setLayout(new BorderLayout());
        setBackground(UIConstants.SIDEBAR_BG);
        setPreferredSize(new Dimension(250, 0));
        add(createSidebarHeader(), BorderLayout.NORTH);
        add(createNavMenu(), BorderLayout.CENTER);
        add(createLogoutSection(), BorderLayout.SOUTH);
    }

    private JPanel createSidebarHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 18, 24, 14));

        JPanel mark = new JPanel(new GridBagLayout());
        mark.setBackground(UIConstants.ACCENT_BLUE);
        mark.setPreferredSize(new Dimension(40, 40));
        JLabel bolt = new JLabel(UIIcons.of(UIIcons.Kind.LIGHTNING));
        bolt.setForeground(new Color(255, 232, 153));
        mark.add(bolt);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("ELECTRICITY BILLING");
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("ADMIN WORKSPACE");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subtitle.setForeground(new Color(151, 171, 194));
        brand.add(name);
        brand.add(Box.createVerticalStrut(5));
        brand.add(subtitle);
        header.add(mark, BorderLayout.WEST);
        header.add(brand, BorderLayout.CENTER);
        return header;
    }

    private JPanel createNavMenu() {
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(0, 12, 8, 12));

        JLabel section = new JLabel("WORKSPACE");
        section.setFont(UIConstants.FONT_BADGE);
        section.setForeground(new Color(119, 141, 165));
        section.setBorder(new EmptyBorder(2, 14, 10, 0));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(section);

        addNavButton(menu, "Dashboard", UIIcons.Kind.DASHBOARD, VIEW_DASHBOARD);
        addNavButton(menu, "Consumers", UIIcons.Kind.CONSUMERS, VIEW_CONSUMER_MANAGEMENT);
        addNavButton(menu, "Bill generation", UIIcons.Kind.BILL, VIEW_BILL_GENERATION);
        addNavButton(menu, "Bill history", UIIcons.Kind.HISTORY, VIEW_BILL_HISTORY);
        addNavButton(menu, "Payments", UIIcons.Kind.PAYMENT, VIEW_PAYMENTS);
        addNavButton(menu, "Reports", UIIcons.Kind.REPORTS, VIEW_REPORTS);
        addNavButton(menu, "Profile", UIIcons.Kind.PROFILE, VIEW_PROFILE);
        setSelected(navButtons.get(0));
        return menu;
    }

    private void addNavButton(JPanel menu, String title, UIIcons.Kind icon, String viewKey) {
        SidebarNavButton button = new SidebarNavButton(title, UIIcons.of(icon));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(event -> {
            setSelected(button);
            if (navListener != null) navListener.onNavigate(viewKey);
        });
        navButtons.add(button);
        menu.add(button);
        menu.add(Box.createVerticalStrut(4));
    }

    private JPanel createLogoutSection() {
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(10, 12, 18, 12));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(48, 67, 88));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottom.add(separator);
        bottom.add(Box.createVerticalStrut(10));

        SidebarNavButton logout = new SidebarNavButton("Sign out", UIIcons.of(UIIcons.Kind.LOGOUT), true);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(event -> handleLogout());
        bottom.add(logout);
        return bottom;
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Sign out of the administrator workspace?", "Confirm sign out",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION && navListener != null) navListener.onLogout();
    }

    private void setSelected(SidebarNavButton target) {
        if (selectedButton != null) selectedButton.setActive(false);
        selectedButton = target;
        if (selectedButton != null) selectedButton.setActive(true);
    }
}
