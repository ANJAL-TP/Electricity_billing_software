package com.electricity.userdashboard;

import com.electricity.auth.ui.SidebarNavButton;
import com.electricity.auth.ui.UIConstants;
import com.electricity.auth.ui.UIIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Dark, keyboard-accessible navigation for the consumer workspace. */
public class UserSidebarPanel extends JPanel {
    public interface NavigationListener {
        void onNavigate(String viewKey);
        void onLogout();
    }

    public static final String VIEW_DASHBOARD = "USER_DASHBOARD";
    public static final String VIEW_BILL = "USER_BILL";
    public static final String VIEW_HISTORY = "USER_HISTORY";
    public static final String VIEW_PAYMENT = "USER_PAYMENT";
    public static final String VIEW_PROFILE = "USER_PROFILE";

    private final NavigationListener listener;
    private final List<SidebarNavButton> navButtons = new ArrayList<>();
    private SidebarNavButton selectedButton;

    public UserSidebarPanel(NavigationListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UIConstants.SIDEBAR_BG);
        setPreferredSize(new Dimension(250, 0));
        add(createHeader(), BorderLayout.NORTH);
        add(createMenu(), BorderLayout.CENTER);
        add(createLogout(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
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
        JLabel subtitle = new JLabel("CONSUMER PORTAL");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subtitle.setForeground(new Color(151, 171, 194));
        brand.add(name);
        brand.add(Box.createVerticalStrut(5));
        brand.add(subtitle);
        header.add(mark, BorderLayout.WEST);
        header.add(brand, BorderLayout.CENTER);
        return header;
    }

    private JPanel createMenu() {
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(0, 12, 8, 12));

        JLabel section = new JLabel("MY ACCOUNT");
        section.setFont(UIConstants.FONT_BADGE);
        section.setForeground(new Color(119, 141, 165));
        section.setBorder(new EmptyBorder(2, 14, 10, 0));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(section);

        addButton(menu, "Dashboard", UIIcons.Kind.DASHBOARD, VIEW_DASHBOARD);
        addButton(menu, "My bill", UIIcons.Kind.BILL, VIEW_BILL);
        addButton(menu, "Bill history", UIIcons.Kind.HISTORY, VIEW_HISTORY);
        addButton(menu, "Payments", UIIcons.Kind.PAYMENT, VIEW_PAYMENT);
        addButton(menu, "Profile", UIIcons.Kind.PROFILE, VIEW_PROFILE);
        setSelected(navButtons.get(0));
        return menu;
    }

    private void addButton(JPanel menu, String title, UIIcons.Kind icon, String viewKey) {
        SidebarNavButton button = new SidebarNavButton(title, UIIcons.of(icon));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(event -> {
            setSelected(button);
            listener.onNavigate(viewKey);
        });
        navButtons.add(button);
        menu.add(button);
        menu.add(Box.createVerticalStrut(4));
    }

    private JPanel createLogout() {
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
        logout.addActionListener(event -> listener.onLogout());
        bottom.add(logout);
        return bottom;
    }

    private void setSelected(SidebarNavButton target) {
        if (selectedButton != null) selectedButton.setActive(false);
        selectedButton = target;
        if (selectedButton != null) selectedButton.setActive(true);
    }
}
