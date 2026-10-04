package com.electricity.admindashboard;

import com.electricity.admindashboard.service.PaymentBill;
import com.electricity.admindashboard.service.PaymentManagementService;
import com.electricity.admindashboard.service.PaymentRecord;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Administrator payment entry for cash received and confirmation of consumer payment requests. */
public final class AdminPaymentPanel extends JPanel {
    private final PaymentManagementService service;
    private final JTextField billNumber = new JTextField();
    private final JTextField amount = new JTextField();
    private final JTextField reference = new JTextField();
    private final JComboBox<String> method = new JComboBox<>(new String[]{"CASH", "UPI", "CARD", "BANK_TRANSFER"});
    private final JLabel billInfo = new JLabel("Search for a bill to see the outstanding balance.");
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Bill", "Consumer", "Name", "Amount", "Method", "Reference", "Status", "Payment Date"
    }, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
    private final JTable table = new JTable(model);
    private PaymentBill selectedBill;

    public AdminPaymentPanel(PaymentManagementService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 18));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 20, 28));
        add(title(), BorderLayout.NORTH);
        add(body(), BorderLayout.CENTER);
        refreshPayments();
    }

    private JComponent title() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("ADMINISTRATOR  /  COLLECTIONS");
        eyebrow.setFont(UIConstants.FONT_BADGE);
        eyebrow.setForeground(UIConstants.TEXT_MUTED);
        JLabel title = new JLabel("Payments");
        title.setFont(UIConstants.FONT_PAGE_TITLE);
        title.setForeground(UIConstants.TEXT_DARK);
        JLabel subtitle = new JLabel("Record confirmed payments and review recent transactions.");
        subtitle.setFont(UIConstants.FONT_SUBTITLE);
        subtitle.setForeground(UIConstants.TEXT_MUTED);
        panel.add(eyebrow);
        panel.add(Box.createVerticalStrut(4));
        panel.add(title);
        panel.add(Box.createVerticalStrut(3));
        panel.add(subtitle);
        return panel;
    }

    private JComponent body() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        row(form, c, 0, "Bill number", billNumber);
        JButton search = new JButton("Find Bill");
        UIConstants.styleButton(search, Color.WHITE, UIConstants.PRIMARY_NAVY);
        search.addActionListener(event -> findBill());
        c.gridx = 2; c.gridy = 0; c.weightx = 0;
        form.add(search, c);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 3;
        billInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        billInfo.setForeground(UIConstants.PRIMARY_NAVY);
        form.add(billInfo, c);
        row(form, c, 2, "Amount received", amount);
        row(form, c, 3, "Payment method", method);
        row(form, c, 4, "Transaction reference (optional)", reference);
        JButton save = new JButton("Record Confirmed Payment");
        UIConstants.styleButton(save, UIConstants.ACCENT_BLUE, Color.WHITE);
        save.addActionListener(event -> savePayment());
        c.gridx = 1; c.gridy = 5; c.gridwidth = 2;
        form.add(save, c);

        table.setAutoCreateRowSorter(true);
        UIConstants.styleTable(table);
        body.add(form, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UIConstants.BORDER_LIGHT));
        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    private static void row(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        if (field instanceof JTextField textField) UIConstants.styleTextField(textField);
        if (field instanceof JComboBox<?> combo) {
            combo.setFont(UIConstants.FONT_INPUT);
            combo.setBackground(Color.WHITE);
        }
        field.setPreferredSize(new Dimension(240, 40));
        form.add(field, c);
        c.gridx = 2; c.weightx = 0;
        form.add(Box.createHorizontalStrut(20), c);
    }

    private void findBill() {
        Optional<PaymentBill> result = service.findBill(billNumber.getText());
        if (result.isEmpty()) {
            selectedBill = null;
            billInfo.setText("Bill not found. Check the bill number and try again.");
            amount.setText("");
            return;
        }
        selectedBill = result.get();
        billInfo.setText(String.format(Locale.ROOT,
                "%s | %s | Period %s | Total INR %s | Paid INR %s | Outstanding INR %s",
                selectedBill.consumerId(), selectedBill.consumerName(), selectedBill.billingPeriod(),
                selectedBill.totalAmount(), selectedBill.paidAmount(), selectedBill.outstandingAmount()));
        amount.setText(selectedBill.outstandingAmount().toPlainString());
    }

    private void savePayment() {
        if (selectedBill == null) {
            JOptionPane.showMessageDialog(this, "Find a bill before recording a payment.", "Bill Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BigDecimal value = new BigDecimal(amount.getText().trim());
            PaymentBill updated = service.recordPayment(selectedBill.billNumber(), value,
                    String.valueOf(method.getSelectedItem()), reference.getText());
            selectedBill = updated;
            billInfo.setText("Saved. Outstanding balance: INR " + updated.outstandingAmount() + " | Status: " + updated.paymentStatus());
            amount.setText(updated.outstandingAmount().toPlainString());
            refreshPayments();
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Enter a valid payment amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this,
                    exception.getMessage() == null ? "Payment could not be recorded." : exception.getMessage(),
                    "Payment Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() { refreshPayments(); }

    private void refreshPayments() {
        List<PaymentRecord> records = service.getRecentPayments();
        model.setRowCount(0);
        for (PaymentRecord record : records) {
            model.addRow(new Object[]{record.billNumber(), record.consumerId(), record.consumerName(), record.amount(),
                    record.paymentMethod(), record.transactionReference(), record.status(), record.paymentDate()});
        }
    }
}
