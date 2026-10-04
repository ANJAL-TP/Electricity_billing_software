package com.electricity.admindashboard;

import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.service.AdminDashboardService;
import com.electricity.config.DatabaseAccessException;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Profile Panel displaying Administrator details with an Edit Profile dialog.
 */
public class ProfilePanel extends JPanel {

    private final AdminDashboardService dashboardService;
    private AdminProfile profile;

    // UI Labels to refresh dynamically
    private JLabel lblAdminIdVal;
    private JLabel lblUsernameVal;
    private JLabel lblFullNameVal;
    private JLabel lblEmailVal;
    private JLabel lblDepartmentVal;
    private JLabel lblLastLoginVal;
    private JLabel lblAvatar;

    public ProfilePanel(AdminDashboardService dashboardService, AdminProfile profile) {
        this.dashboardService = dashboardService;
        this.profile = profile != null ? profile : dashboardService.getAdminProfile("admin");

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
    }

    private void initComponents() {
        // 1. Header Section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblBreadcrumb = new JLabel("ADMINISTRATOR  /  PROFILE");
        lblBreadcrumb.setFont(UIConstants.FONT_BADGE);
        lblBreadcrumb.setForeground(UIConstants.TEXT_MUTED);

        JLabel lblTitle = new JLabel("Administrator profile");
        lblTitle.setFont(UIConstants.FONT_PAGE_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_DARK);

        headerPanel.add(lblBreadcrumb);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Card
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        centerWrapper.setOpaque(false);

        JPanel profileCard = new JPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBackground(UIConstants.CARD_BG);
        profileCard.setPreferredSize(new Dimension(720, 520));
        profileCard.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(24, 30, 24, 30)
        ));

        // Profile Avatar & Badging Row
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAvatar = createAvatarLabel(profile.username());
        avatarRow.add(lblAvatar);

        JPanel nameCol = new JPanel();
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        nameCol.setOpaque(false);

        JLabel lblDisplayName = new JLabel(profile.fullName());
        lblDisplayName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDisplayName.setForeground(UIConstants.TEXT_DARK);

        JLabel lblRoleBadge = new JLabel("SYSTEM ADMINISTRATOR");
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRoleBadge.setForeground(UIConstants.PRIMARY_NAVY);
        lblRoleBadge.setOpaque(true);
        lblRoleBadge.setBackground(new Color(219, 234, 254));
        lblRoleBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        nameCol.add(lblDisplayName);
        nameCol.add(Box.createVerticalStrut(4));
        nameCol.add(lblRoleBadge);
        avatarRow.add(nameCol);

        profileCard.add(avatarRow);
        profileCard.add(Box.createVerticalStrut(20));

        // Horizontal Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.BORDER_LIGHT);
        sep.setMaximumSize(new Dimension(660, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.add(sep);
        profileCard.add(Box.createVerticalStrut(18));

        // Information Grid
        JPanel detailsGrid = new JPanel(new GridLayout(6, 2, 16, 12));
        detailsGrid.setOpaque(false);
        detailsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsGrid.setMaximumSize(new Dimension(660, 240));

        lblAdminIdVal = new JLabel(profile.adminId());
        lblUsernameVal = new JLabel(profile.username());
        lblFullNameVal = new JLabel(profile.fullName());
        lblEmailVal = new JLabel(profile.email());
        lblDepartmentVal = new JLabel(profile.department());
        lblLastLoginVal = new JLabel(profile.lastLogin());

        addDetailRow(detailsGrid, "Admin ID:", lblAdminIdVal);
        addDetailRow(detailsGrid, "Username:", lblUsernameVal);
        addDetailRow(detailsGrid, "Full Name:", lblFullNameVal);
        addDetailRow(detailsGrid, "Official Email:", lblEmailVal);
        addDetailRow(detailsGrid, "Department:", lblDepartmentVal);
        addDetailRow(detailsGrid, "Last Session Login:", lblLastLoginVal);

        profileCard.add(detailsGrid);
        profileCard.add(Box.createVerticalStrut(24));

        // Edit Profile Button
        JButton btnEdit = new JButton("Edit Profile Details");
        UIConstants.styleButton(btnEdit, UIConstants.ACCENT_BLUE, Color.WHITE);
        btnEdit.setPreferredSize(new Dimension(180, 38));
        btnEdit.setMaximumSize(new Dimension(180, 38));
        btnEdit.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnEdit.addActionListener(e -> openEditProfileDialog());
        profileCard.add(btnEdit);

        centerWrapper.add(profileCard);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private void addDetailRow(JPanel grid, String label, JLabel valueLabel) {
        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(UIConstants.FONT_LABEL);
        lblTitle.setForeground(UIConstants.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(UIConstants.TEXT_DARK);

        grid.add(lblTitle);
        grid.add(valueLabel);
    }

    private JLabel createAvatarLabel(String username) {
        String initials = (username != null && username.length() >= 2)
                ? username.substring(0, 2).toUpperCase()
                : "AD";

        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PRIMARY_NAVY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(56, 56));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avatar.setForeground(Color.WHITE);
        return avatar;
    }

    private void openEditProfileDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Administrator Profile", true);
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 14));
        formPanel.setBorder(new EmptyBorder(20, 24, 10, 24));
        formPanel.setBackground(Color.WHITE);

        JTextField txtName = new JTextField(profile.fullName());
        JTextField txtEmail = new JTextField(profile.email());
        UIConstants.styleTextField(txtName);
        UIConstants.styleTextField(txtEmail);

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Email Address:"));
        formPanel.add(txtEmail);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(Color.WHITE);

        JButton btnSave = new JButton("Save Changes");
        UIConstants.styleButton(btnSave, UIConstants.ACCENT_BLUE, Color.WHITE);

        JButton btnCancel = new JButton("Cancel");
        UIConstants.styleButton(btnCancel, Color.WHITE, UIConstants.PRIMARY_NAVY);

        btnSave.addActionListener(e -> {
            String newName = txtName.getText().trim();
            String newEmail = txtEmail.getText().trim();
            if (newName.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Full name and email cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AdminProfile updated = profile.withUpdatedInfo(newName, newEmail, profile.department());
            try {
                if (!dashboardService.updateProfile(updated)) {
                    JOptionPane.showMessageDialog(dialog, "The profile was not found in MySQL.", "Save Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (DatabaseAccessException exception) {
                JOptionPane.showMessageDialog(dialog, exception.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.profile = updated;

            // Update UI labels
            lblFullNameVal.setText(updated.fullName());
            lblEmailVal.setText(updated.email());
            lblDepartmentVal.setText(updated.department());

            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public AdminProfile getProfile() {
        return profile;
    }
}
