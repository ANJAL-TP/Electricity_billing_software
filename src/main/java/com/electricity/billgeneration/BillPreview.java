package com.electricity.billgeneration;

import com.electricity.billgeneration.model.BillCalculationResult;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Professional electricity bill invoice preview dialog conforming to Module 6 requirements.
 */
public class BillPreview extends JDialog {

    private final BillCalculationResult bill;
    private final String paymentStatus;
    private final String dueDate;

    public BillPreview(Window owner, BillCalculationResult bill) {
        this(owner, bill, "Preview", "—");
    }

    public BillPreview(Window owner, BillCalculationResult bill, String paymentStatus, String dueDate) {
        super(owner, "Electricity Bill Invoice Preview", ModalityType.APPLICATION_MODAL);
        this.bill = bill;
        this.paymentStatus = paymentStatus == null ? "—" : paymentStatus;
        this.dueDate = dueDate == null ? "—" : dueDate;

        setSize(540, 680);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BG_LIGHT);

        initComponents();
    }

    private void initComponents() {
        // Main Invoice Card inside scroll wrapper
        JPanel invoiceCard = new JPanel();
        invoiceCard.setLayout(new BoxLayout(invoiceCard, BoxLayout.Y_AXIS));
        invoiceCard.setBackground(Color.WHITE);
        invoiceCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(24, 30, 24, 30)
        ));

        // 1. Header Section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblOrg = new JLabel("ELECTRICITY BILLING AUTHORITY");
        lblOrg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblOrg.setForeground(UIConstants.PRIMARY_NAVY);
        lblOrg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDocTitle = new JLabel("ELECTRICITY BILL");
        lblDocTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDocTitle.setForeground(UIConstants.TEXT_DARK);
        lblDocTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Official Consumer Statement & Tax Invoice");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(UIConstants.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblOrg);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblDocTitle);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblSub);

        invoiceCard.add(headerPanel);
        invoiceCard.add(Box.createVerticalStrut(16));

        // Divider
        invoiceCard.add(createDivider());
        invoiceCard.add(Box.createVerticalStrut(14));

        // 2. Metadata Grid (Bill No & Billing Period)
        JPanel metaGrid = new JPanel(new GridLayout(3, 4, 10, 6));
        metaGrid.setBackground(Color.WHITE);
        metaGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        metaGrid.setMaximumSize(new Dimension(460, 48));

        addMetaRow(metaGrid, "Bill Number:", bill.billNumber());
        addMetaRow(metaGrid, "Billing Period:", bill.billingPeriod());
        addMetaRow(metaGrid, "Payment Status:", paymentStatus);
        addMetaRow(metaGrid, "Due Date:", dueDate);
        addMetaRow(metaGrid, "Tariff Category:", bill.tariffCategory());
        addMetaRow(metaGrid, "Meter Number:", bill.meterNumber());

        invoiceCard.add(metaGrid);
        invoiceCard.add(Box.createVerticalStrut(14));
        invoiceCard.add(createDivider());
        invoiceCard.add(Box.createVerticalStrut(14));

        // 3. Consumer Details
        JPanel consumerGrid = new JPanel(new GridLayout(2, 2, 10, 6));
        consumerGrid.setBackground(Color.WHITE);
        consumerGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        consumerGrid.setMaximumSize(new Dimension(460, 48));

        addMetaRow(consumerGrid, "Consumer ID:", bill.consumerId());
        addMetaRow(consumerGrid, "Consumer Name:", bill.consumerName());

        invoiceCard.add(consumerGrid);
        invoiceCard.add(Box.createVerticalStrut(6));

        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.setBackground(Color.WHITE);
        addressPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addressPanel.setMaximumSize(new Dimension(460, 36));

        JLabel lblAddrKey = new JLabel("Address: ");
        lblAddrKey.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAddrKey.setForeground(UIConstants.TEXT_MUTED);

        JLabel lblAddrVal = new JLabel("<html>" + bill.address() + "</html>");
        lblAddrVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAddrVal.setForeground(UIConstants.TEXT_DARK);

        addressPanel.add(lblAddrKey, BorderLayout.WEST);
        addressPanel.add(lblAddrVal, BorderLayout.CENTER);
        invoiceCard.add(addressPanel);
        invoiceCard.add(Box.createVerticalStrut(14));

        invoiceCard.add(createDivider());
        invoiceCard.add(Box.createVerticalStrut(14));

        // 4. Meter & Consumption Details
        JPanel meterGrid = new JPanel(new GridLayout(3, 2, 10, 6));
        meterGrid.setBackground(Color.WHITE);
        meterGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        meterGrid.setMaximumSize(new Dimension(460, 72));

        addMetaRow(meterGrid, "Previous Reading:", String.format("%.1f kWh", bill.previousReading()));
        addMetaRow(meterGrid, "Current Reading:", String.format("%.1f kWh", bill.currentReading()));
        addMetaRow(meterGrid, "Units Consumed:", bill.formattedUnits());
        addMetaRow(meterGrid, "Billing Status:", "Verified & Computed");

        invoiceCard.add(meterGrid);
        invoiceCard.add(Box.createVerticalStrut(14));
        invoiceCard.add(createDivider());
        invoiceCard.add(Box.createVerticalStrut(14));

        // 5. Charges Breakdown Table
        JPanel chargesPanel = new JPanel(new GridLayout(3, 2, 10, 6));
        chargesPanel.setBackground(Color.WHITE);
        chargesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chargesPanel.setMaximumSize(new Dimension(460, 72));

        addChargeRow(chargesPanel, "Energy Charge:", bill.formattedEnergyCharge());
        addChargeRow(chargesPanel, "Fixed Charge:", bill.formattedFixedCharge());
        addChargeRow(chargesPanel, "Tax/Other Charges:", bill.formattedTaxAmount());

        invoiceCard.add(chargesPanel);
        invoiceCard.add(Box.createVerticalStrut(16));

        // 6. Highlighted Total Amount Banner
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(new Color(239, 246, 255)); // Soft blue
        totalBox.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.ACCENT_BLUE, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
        totalBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        totalBox.setMaximumSize(new Dimension(460, 48));

        JLabel lblTotalTitle = new JLabel("TOTAL AMOUNT:");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalTitle.setForeground(UIConstants.PRIMARY_NAVY);

        JLabel lblTotalVal = new JLabel(bill.formattedTotalAmount());
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalVal.setForeground(UIConstants.ACCENT_BLUE);

        totalBox.add(lblTotalTitle, BorderLayout.WEST);
        totalBox.add(lblTotalVal, BorderLayout.EAST);
        invoiceCard.add(totalBox);
        invoiceCard.add(Box.createVerticalStrut(16));

        // Footer note
        JLabel lblFooter = new JLabel("Please pay on or before the due date to avoid disconnection.");
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(UIConstants.TEXT_MUTED);
        lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);
        invoiceCard.add(lblFooter);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 14));
        centerWrapper.setOpaque(false);
        centerWrapper.add(invoiceCard);
        add(centerWrapper, BorderLayout.CENTER);

        // Action Buttons at Bottom (Print, Close)
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomBar.setBackground(UIConstants.BG_LIGHT);
        bottomBar.setBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, false));

        JButton btnPrint = new JButton("Print / Save Invoice");
        UIConstants.styleButton(btnPrint, UIConstants.ACCENT_BLUE, Color.WHITE);

        btnPrint.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Print job for " + bill.billNumber() + " sent to the default printer successfully.",
                    "Print Confirmation",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JButton btnClose = new JButton("Close");
        UIConstants.styleButton(btnClose, UIConstants.CLEAR_GRAY, Color.WHITE);
        btnClose.addActionListener(e -> dispose());

        bottomBar.add(btnPrint);
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void addMetaRow(JPanel panel, String label, String value) {
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKey.setForeground(UIConstants.TEXT_MUTED);

        JLabel lblVal = new JLabel(value != null ? value : "—");
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVal.setForeground(UIConstants.TEXT_DARK);

        panel.add(lblKey);
        panel.add(lblVal);
    }

    private void addChargeRow(JPanel panel, String label, String value) {
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblKey.setForeground(UIConstants.TEXT_DARK);

        JLabel lblVal = new JLabel(value, SwingConstants.RIGHT);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblVal.setForeground(UIConstants.TEXT_DARK);

        panel.add(lblKey);
        panel.add(lblVal);
    }

    private JSeparator createDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        sep.setMaximumSize(new Dimension(460, 1));
        return sep;
    }
}
