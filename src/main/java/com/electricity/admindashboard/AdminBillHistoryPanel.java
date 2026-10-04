package com.electricity.admindashboard;

import com.electricity.admindashboard.service.BillHistoryRecord;
import com.electricity.admindashboard.service.BillHistoryService;
import com.electricity.auth.ui.UIConstants;
import com.electricity.billgeneration.BillPreview;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Administrator bill history with search, period/status filters, and invoice preview. */
public final class AdminBillHistoryPanel extends JPanel {
    private final BillHistoryService service;
    private final JTextField search = new JTextField();
    private final JComboBox<String> periods = new JComboBox<>();
    private final JComboBox<String> statuses = new JComboBox<>(new String[]{"ALL", "UNPAID", "PARTIALLY_PAID", "PAYMENT_PENDING", "PAID", "OVERDUE"});
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Bill Number", "Consumer ID", "Consumer Name", "Period", "Units", "Total Amount", "Generated", "Due Date", "Status"
    }, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
    private final JTable table = new JTable(model);
    private List<BillHistoryRecord> visibleRecords = List.of();

    public AdminBillHistoryPanel(BillHistoryService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 18));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(title(), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);
        periods.addItem("ALL");
        service.getBillingPeriods().forEach(periods::addItem);
        refresh();
    }

    private JComponent title() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("ADMINISTRATOR  /  BILLING");
        eyebrow.setFont(UIConstants.FONT_BADGE);
        eyebrow.setForeground(UIConstants.TEXT_MUTED);
        JLabel title = new JLabel("Bill history");
        title.setFont(UIConstants.FONT_PAGE_TITLE);
        title.setForeground(UIConstants.TEXT_DARK);
        JLabel subtitle = new JLabel("Review billing periods, payment status, and invoice details.");
        subtitle.setFont(UIConstants.FONT_SUBTITLE);
        subtitle.setForeground(UIConstants.TEXT_MUTED);
        panel.add(eyebrow);
        panel.add(Box.createVerticalStrut(4));
        panel.add(title);
        panel.add(Box.createVerticalStrut(3));
        panel.add(subtitle);
        return panel;
    }

    private JComponent content() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 9));
        filters.setBackground(Color.WHITE);
        filters.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        UIConstants.styleTextField(search);
        search.setPreferredSize(new Dimension(220, 40));
        filters.add(label("Search"));
        filters.add(search);
        filters.add(label("Period"));
        styleCombo(periods);
        filters.add(periods);
        filters.add(label("Status"));
        styleCombo(statuses);
        filters.add(statuses);
        JButton apply = new JButton("Apply Filters");
        UIConstants.styleButton(apply, UIConstants.ACCENT_BLUE, Color.WHITE);
        apply.addActionListener(event -> refresh());
        JButton view = new JButton("View / Print Bill");
        UIConstants.styleButton(view, Color.WHITE, UIConstants.PRIMARY_NAVY);
        view.addActionListener(event -> previewSelected());
        filters.add(apply);
        filters.add(view);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIConstants.styleTable(table);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) previewSelected();
            }
        });
        panel.add(filters, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UIConstants.BORDER_LIGHT));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BADGE);
        label.setForeground(UIConstants.TEXT_MUTED);
        return label;
    }

    private static void styleCombo(JComboBox<String> combo) {
        combo.setFont(UIConstants.FONT_MESSAGE);
        combo.setBackground(Color.WHITE);
        combo.setBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true));
        combo.setPreferredSize(new Dimension(160, 40));
    }

    private void refresh() {
        String period = String.valueOf(periods.getSelectedItem());
        String status = String.valueOf(statuses.getSelectedItem());
        visibleRecords = service.search(search.getText(), period, status);
        model.setRowCount(0);
        for (BillHistoryRecord record : visibleRecords) {
            model.addRow(new Object[]{record.bill().billNumber(), record.bill().consumerId(), record.consumerName(),
                    record.bill().billingPeriod(), record.bill().formattedUnits(), record.bill().formattedTotalAmount(),
                    record.generatedAt(), record.dueDate(), record.paymentStatus()});
        }
    }

    public void refreshData() { refresh(); }

    private void previewSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int row = table.convertRowIndexToModel(viewRow);
        BillHistoryRecord record = visibleRecords.get(row);
        new BillPreview(SwingUtilities.getWindowAncestor(this), record.bill(),
                record.paymentStatus(), record.dueDate()).setVisible(true);
    }
}
