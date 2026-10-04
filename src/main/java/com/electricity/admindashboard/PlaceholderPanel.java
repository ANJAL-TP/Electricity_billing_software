package com.electricity.admindashboard;

import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Reusable placeholder panel for modules currently under development.
 * Demonstrates navigation flow and shows the database tables/schema intended for that module.
 */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String moduleName, String iconSymbol, String description, String targetTable) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Breadcrumb & Title
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblBreadcrumb = new JLabel("Admin Portal  ›  " + moduleName);
        lblBreadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBreadcrumb.setForeground(UIConstants.TEXT_MUTED);

        JLabel lblTitle = new JLabel(iconSymbol + "  " + moduleName);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIConstants.TEXT_DARK);

        headerPanel.add(lblBreadcrumb);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center Showcase Card
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);

        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(UIConstants.CARD_BG);
        infoCard.setPreferredSize(new Dimension(640, 360));
        infoCard.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(32, 40, 32, 40)
        ));

        JLabel lblIcon = new JLabel(iconSymbol);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblCardTitle = new JLabel(moduleName);
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblCardTitle.setForeground(UIConstants.TEXT_DARK);
        lblCardTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><center>" + description + "</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(UIConstants.TEXT_MUTED);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Status Badge Panel
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        badgePanel.setOpaque(false);
        badgePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStatusBadge = new JLabel("  ● UI READY  |  AWAITING JDBC DATABASE CONNECTION  ");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatusBadge.setForeground(new Color(30, 64, 175));
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(219, 234, 254));
        lblStatusBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badgePanel.add(lblStatusBadge);

        // Schema Target Notice
        JPanel schemaNotice = new JPanel(new FlowLayout(FlowLayout.CENTER));
        schemaNotice.setOpaque(false);
        schemaNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSchema = new JLabel("Target Backend Table: " + targetTable);
        lblSchema.setFont(new Font("Consolas", Font.BOLD, 12));
        lblSchema.setForeground(UIConstants.PRIMARY_NAVY);
        schemaNotice.add(lblSchema);

        infoCard.add(lblIcon);
        infoCard.add(Box.createVerticalStrut(14));
        infoCard.add(lblCardTitle);
        infoCard.add(Box.createVerticalStrut(10));
        infoCard.add(lblDesc);
        infoCard.add(Box.createVerticalStrut(18));
        infoCard.add(badgePanel);
        infoCard.add(Box.createVerticalStrut(12));
        infoCard.add(schemaNotice);

        cardWrapper.add(infoCard);
        add(cardWrapper, BorderLayout.CENTER);
    }
}
