package com.electricity.admindashboard;

import com.electricity.admindashboard.model.ManagedConsumer;
import com.electricity.admindashboard.service.ConsumerManagementService;
import com.electricity.admindashboard.service.MockConsumerManagementService;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Administrator screen for searching, creating, editing and deactivating consumers. */
public class ConsumerManagementPanel extends JPanel {
    private final ConsumerManagementService consumerService;
    private final JTextField txtId = field();
    private final JTextField txtName = field();
    private final JTextField txtAddress = field();
    private final JTextField txtPhone = field();
    private final JTextField txtEmail = field();
    private final JTextField txtMeter = field();
    private final JTextField txtSearch = field();
    private final JComboBox<String> cmbTariff = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Consumer ID", "Name", "Meter", "Tariff", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private List<ManagedConsumer> visibleRecords = List.of();
    private ManagedConsumer selectedConsumer;
    private String originalConsumerId;
    private JLabel recordCountLabel;
    private JButton toggleButton;

    public ConsumerManagementPanel() {
        this(new MockConsumerManagementService());
    }

    public ConsumerManagementPanel(ConsumerManagementService consumerService) {
        this.consumerService = consumerService;
        setLayout(new BorderLayout(0, 18));
        setBackground(UIConstants.BG_LIGHT);
        setBorder(new EmptyBorder(22, 26, 20, 26));
        add(createTitle(), BorderLayout.NORTH);
        JScrollPane pageScroll = new JScrollPane(createMainContent());
        pageScroll.setBorder(null);
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(pageScroll, BorderLayout.CENTER);
        loadTariffOptions();
        refreshTable("");
        clearForm();
    }

    private JPanel createTitle() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("ADMINISTRATOR  /  ACCOUNTS");
        eyebrow.setFont(UIConstants.FONT_BADGE);
        eyebrow.setForeground(UIConstants.TEXT_MUTED);
        JLabel title = new JLabel("Consumers");
        title.setFont(UIConstants.FONT_PAGE_TITLE);
        title.setForeground(UIConstants.TEXT_DARK);
        JLabel subtitle = new JLabel("Create, search, and manage electricity accounts.");
        subtitle.setFont(UIConstants.FONT_SUBTITLE);
        subtitle.setForeground(UIConstants.TEXT_MUTED);
        panel.add(eyebrow);
        panel.add(Box.createVerticalStrut(4));
        panel.add(title);
        panel.add(Box.createVerticalStrut(3));
        panel.add(subtitle);
        return panel;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(createDetailsCard());
        content.add(Box.createVerticalStrut(14));
        content.add(createRecordsCard());
        return content;
    }

    private JPanel createDetailsCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(sectionTitle("Consumer details", "ACCOUNT INFORMATION"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 20, 10));
        form.setOpaque(false);
        form.add(labeledField("Consumer ID", txtId));
        form.add(labeledField("Consumer name", txtName));
        form.add(labeledField("Address", txtAddress));
        form.add(labeledField("Phone number", txtPhone));
        form.add(labeledField("Email address", txtEmail));
        form.add(labeledField("Meter number", txtMeter));
        form.add(labeledField("Tariff", cmbTariff));
        form.add(new JLabel(" "));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        actions.setOpaque(false);
        JButton add = actionButton("Add consumer", UIConstants.ACCENT_BLUE, Color.WHITE);
        JButton update = actionButton("Save changes", Color.WHITE, UIConstants.PRIMARY_NAVY);
        JButton clear = actionButton("Clear form", Color.WHITE, UIConstants.PRIMARY_NAVY);
        toggleButton = actionButton("Deactivate", new Color(255, 245, 245), new Color(166, 45, 54));
        toggleButton.setEnabled(false);
        add.addActionListener(e -> addConsumer());
        update.addActionListener(e -> updateConsumer());
        clear.addActionListener(e -> clearForm());
        toggleButton.addActionListener(e -> toggleConsumerActive());
        actions.add(add);
        actions.add(update);
        actions.add(clear);
        actions.add(toggleButton);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(form, BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRecordsCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(0, 320));
        card.add(sectionTitle("Consumer records", "DIRECTORY"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        JLabel label = new JLabel("Search accounts");
        label.setFont(UIConstants.FONT_LABEL);
        label.setForeground(UIConstants.TEXT_DARK);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setToolTipText("Search by ID, name, phone, address, meter or tariff");
        JButton search = actionButton("Search", UIConstants.ACCENT_BLUE, Color.WHITE);
        search.addActionListener(e -> refreshTable(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> refreshTable(txtSearch.getText().trim()));
        searchRow.add(label, BorderLayout.WEST);
        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(search, BorderLayout.EAST);
        body.add(searchRow, BorderLayout.NORTH);

        UIConstants.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) return;
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow < 0 || modelRow >= visibleRecords.size()) return;
            selectedConsumer = visibleRecords.get(modelRow);
            originalConsumerId = selectedConsumer.consumerId();
            loadSelected();
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UIConstants.BORDER_LIGHT));
        body.add(scrollPane, BorderLayout.CENTER);
        recordCountLabel = new JLabel();
        recordCountLabel.setFont(UIConstants.FONT_MESSAGE);
        recordCountLabel.setForeground(UIConstants.TEXT_MUTED);
        body.add(recordCountLabel, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void loadTariffOptions() {
        String selected = (String) cmbTariff.getSelectedItem();
        cmbTariff.removeAllItems();
        for (String code : consumerService.getTariffCodes()) cmbTariff.addItem(code);
        cmbTariff.setFont(UIConstants.FONT_INPUT);
        cmbTariff.setBackground(Color.WHITE);
        cmbTariff.setPreferredSize(new Dimension(180, 40));
        if (selected != null) cmbTariff.setSelectedItem(selected);
        if (cmbTariff.getSelectedIndex() < 0 && cmbTariff.getItemCount() > 0) cmbTariff.setSelectedIndex(0);
    }

    private void refreshTable(String query) {
        try {
            visibleRecords = consumerService.search(query);
            tableModel.setRowCount(0);
            for (ManagedConsumer consumer : visibleRecords) {
                tableModel.addRow(new Object[]{consumer.consumerId(), consumer.name(), consumer.meterNumber(),
                        consumer.tariffCode(), consumer.status()});
            }
            if (recordCountLabel != null) {
                recordCountLabel.setText(visibleRecords.size() + (visibleRecords.size() == 1
                        ? " account" : " accounts"));
            }
        } catch (RuntimeException exception) {
            if (!isDisplayable()) throw exception;
            showOperationError(exception);
        }
    }

    private void addConsumer() {
        if (!validForm()) return;
        try {
            consumerService.create(readForm("ACTIVE"));
            refreshTable(txtSearch.getText().trim());
            clearForm();
            JOptionPane.showMessageDialog(this, "Consumer account created successfully.", "Consumer added", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException exception) {
            showOperationError(exception);
        }
    }

    private void updateConsumer() {
        if (selectedConsumer == null || originalConsumerId == null) {
            JOptionPane.showMessageDialog(this, "Select an account from the table before saving changes.", "Select an account", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validForm()) return;
        try {
            consumerService.update(originalConsumerId, readForm(selectedConsumer.status()));
            refreshTable(txtSearch.getText().trim());
            clearForm();
            JOptionPane.showMessageDialog(this, "Consumer changes saved successfully.", "Changes saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException exception) {
            showOperationError(exception);
        }
    }

    private boolean validForm() {
        if (txtId.getText().isBlank() || txtName.getText().isBlank() || txtMeter.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Consumer ID, name, and meter number are required.", "Required information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        if (!phone.isEmpty() && !phone.matches("[+0-9() .-]{7,20}")) {
            JOptionPane.showMessageDialog(this, "Enter a valid phone number.", "Check phone number", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.", "Check email address", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cmbTariff.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Choose an active tariff before saving.", "Tariff required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void toggleConsumerActive() {
        if (selectedConsumer == null) return;
        boolean activate = !"ACTIVE".equalsIgnoreCase(selectedConsumer.status());
        String action = activate ? "activate" : "deactivate";
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to " + action + " this consumer?",
                "Confirm status change", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            consumerService.setActive(selectedConsumer.consumerId(), activate);
            refreshTable(txtSearch.getText().trim());
            clearForm();
        } catch (RuntimeException exception) {
            showOperationError(exception);
        }
    }

    private void loadSelected() {
        if (selectedConsumer == null) return;
        txtId.setText(selectedConsumer.consumerId());
        txtName.setText(selectedConsumer.name());
        txtAddress.setText(selectedConsumer.address());
        txtPhone.setText(selectedConsumer.phone());
        txtEmail.setText(selectedConsumer.email());
        txtMeter.setText(selectedConsumer.meterNumber());
        cmbTariff.setSelectedItem(selectedConsumer.tariffCode());
        boolean active = "ACTIVE".equalsIgnoreCase(selectedConsumer.status());
        toggleButton.setText(active ? "Deactivate" : "Activate");
        toggleButton.setEnabled(true);
    }

    private ManagedConsumer readForm(String status) {
        return new ManagedConsumer(txtId.getText().trim(), txtName.getText().trim(), txtAddress.getText().trim(),
                txtPhone.getText().trim(), txtEmail.getText().trim(), txtMeter.getText().trim(),
                String.valueOf(cmbTariff.getSelectedItem()), status);
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtAddress.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtMeter.setText("");
        selectedConsumer = null;
        originalConsumerId = null;
        if (toggleButton != null) {
            toggleButton.setText("Deactivate");
            toggleButton.setEnabled(false);
        }
        if (table != null) table.clearSelection();
    }

    private void showOperationError(RuntimeException exception) {
        String message = exception.getMessage() == null ? "The operation could not be completed." : exception.getMessage();
        JOptionPane.showMessageDialog(this, message, "Database operation failed", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(15, 17, 13, 17)));
        return panel;
    }

    private JPanel sectionTitle(String text, String eyebrow) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel kicker = new JLabel(eyebrow);
        kicker.setFont(UIConstants.FONT_BADGE);
        kicker.setForeground(UIConstants.TEXT_MUTED);
        JLabel title = new JLabel(text);
        title.setFont(UIConstants.FONT_SECTION_TITLE);
        title.setForeground(UIConstants.TEXT_DARK);
        panel.add(kicker);
        panel.add(Box.createVerticalStrut(3));
        panel.add(title);
        return panel;
    }

    private JPanel labeledField(String text, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BADGE);
        label.setForeground(UIConstants.TEXT_MUTED);
        label.setBorder(new EmptyBorder(0, 0, 5, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(field);
        return panel;
    }

    private static JTextField field() {
        JTextField field = new JTextField();
        UIConstants.styleTextField(field);
        return field;
    }

    private JButton actionButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        UIConstants.styleButton(button, background, foreground);
        button.setPreferredSize(new Dimension(text.length() > 12 ? 142 : 112, 39));
        return button;
    }
}
