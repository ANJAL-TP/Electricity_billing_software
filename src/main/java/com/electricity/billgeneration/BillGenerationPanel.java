package com.electricity.billgeneration;

import com.electricity.billgeneration.model.BillCalculationResult;
import com.electricity.billgeneration.model.ConsumerSummary;
import com.electricity.billgeneration.service.BillService;
import com.electricity.billgeneration.service.MockBillService;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Main Form and Control Panel for Module 6 — Bill Generation.
 * Provides Consumer lookup, Meter reading input, automated tariff calculation, and invoice preview.
 */
public class BillGenerationPanel extends JPanel {

    private final BillService billService;
    private ConsumerSummary currentConsumer;
    private BillCalculationResult lastCalculatedBill;
    private boolean billAlreadyGenerated;
    private boolean suppressDialogs = false;

    public void setSuppressDialogs(boolean suppress) {
        this.suppressDialogs = suppress;
    }

    public boolean isSuppressDialogs() {
        return suppressDialogs;
    }

    private void showMessage(String message, String title, int messageType) {
        if (!suppressDialogs && !GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(this, message, title, messageType);
        }
    }

    // --- Consumer Details Components ---
    private JTextField txtConsumerId;
    private JButton btnSearchConsumer;
    private JTextField txtConsumerName;
    private JTextField txtAddress;
    private JTextField txtMeterNumber;

    // --- Meter Details Components ---
    private JTextField txtPrevReading;
    private JTextField txtCurrReading;
    private JTextField txtUnitsConsumed;

    // --- Bill Details Components ---
    private JTextField txtBillNumber;
    private JComboBox<String> cmbBillingPeriod;
    private JTextField txtTariff;
    private JTextField txtEnergyCharge;
    private JTextField txtFixedCharge;
    private JTextField txtTaxCharge;
    private JTextField txtTotalAmount;

    // --- Action Buttons ---
    private JButton btnCalculate;
    private JButton btnGenerateBill;
    private JButton btnClear;
    private JButton btnPrintPreview;

    public BillGenerationPanel() {
        this(new MockBillService());
    }

    public BillGenerationPanel(BillService billService) {
        this.billService = billService;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);

        // Content wrapper with scroll capability
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UIConstants.BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Top Header Banner
        mainContent.add(createHeaderPanel());
        mainContent.add(Box.createVerticalStrut(14));

        // 2. Quick Demo Selector Bar
        mainContent.add(createQuickSelectBar());
        mainContent.add(Box.createVerticalStrut(14));

        // 3. Main Form Grid (Left: Consumer & Meter, Right: Bill Details)
        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 490));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        leftCol.add(createConsumerDetailsCard());
        leftCol.add(Box.createVerticalStrut(12));
        leftCol.add(createMeterDetailsCard());
        gridPanel.add(leftCol);

        gridPanel.add(createBillDetailsCard());
        mainContent.add(gridPanel);
        mainContent.add(Box.createVerticalStrut(16));

        // 4. Bottom Action Buttons Bar
        mainContent.add(createButtonBar());

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        setupEvents();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblBreadcrumb = new JLabel("BILLING OPERATIONS  /  METER READING");
        lblBreadcrumb.setFont(UIConstants.FONT_BADGE);
        lblBreadcrumb.setForeground(UIConstants.TEXT_MUTED);

        JLabel lblTitle = new JLabel("Electricity bill generation");
        lblTitle.setFont(UIConstants.FONT_PAGE_TITLE);
        lblTitle.setForeground(UIConstants.TEXT_DARK);

        titleBox.add(lblBreadcrumb);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblTitle);

        JPanel statusBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        statusBadge.setOpaque(false);

        JLabel lblReady = new JLabel("READY FOR BILLING CYCLE");
        lblReady.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblReady.setForeground(new Color(30, 64, 175));
        lblReady.setOpaque(true);
        lblReady.setBackground(new Color(219, 234, 254));
        lblReady.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        statusBadge.add(lblReady);

        header.add(titleBox, BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);
        return header;
    }

    private JPanel createQuickSelectBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(2, 10, 2, 10)
        ));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel lblHint = new JLabel("QUICK SELECT");
        lblHint.setFont(UIConstants.FONT_BADGE);
        lblHint.setForeground(UIConstants.PRIMARY_NAVY);
        bar.add(lblHint);

        for (ConsumerSummary cs : billService.getSampleConsumers()) {
            JButton chip = new JButton(cs.consumerId() + " (" + cs.fullName().split(" ")[0] + ")");
            UIConstants.styleButton(chip, new Color(241, 245, 249), UIConstants.TEXT_DARK);
            chip.addActionListener(e -> {
                txtConsumerId.setText(cs.consumerId());
                searchConsumer();
            });
            bar.add(chip);
        }

        return bar;
    }

    private JPanel createConsumerDetailsCard() {
        JPanel card = createCardContainer("1. Consumer Details");
        card.setPreferredSize(new Dimension(480, 220));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Row 0: Consumer ID + Search Button
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Consumer ID: *"), gbc);

        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setOpaque(false);
        txtConsumerId = createTextField(true);
        txtConsumerId.setPreferredSize(new Dimension(140, 32));

        btnSearchConsumer = new JButton("Search Consumer");
        UIConstants.styleButton(btnSearchConsumer, UIConstants.ACCENT_BLUE, Color.WHITE);

        searchBox.add(txtConsumerId, BorderLayout.CENTER);
        searchBox.add(btnSearchConsumer, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        formGrid.add(searchBox, gbc);

        // Row 1: Consumer Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Consumer Name:"), gbc);
        txtConsumerName = createTextField(false);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        formGrid.add(txtConsumerName, gbc);

        // Row 2: Address
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Address:"), gbc);
        txtAddress = createTextField(false);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        formGrid.add(txtAddress, gbc);

        // Row 3: Meter Number
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Meter Number:"), gbc);
        txtMeterNumber = createTextField(false);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        formGrid.add(txtMeterNumber, gbc);

        card.add(formGrid);
        return card;
    }

    private JPanel createMeterDetailsCard() {
        JPanel card = createCardContainer("2. Meter Details");
        card.setPreferredSize(new Dimension(480, 180));

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        formGrid.setOpaque(false);

        // Row 1: Previous Reading
        formGrid.add(createFieldLabel("Previous Meter Reading: *"));
        txtPrevReading = createTextField(false);
        formGrid.add(txtPrevReading);

        // Row 2: Current Reading
        formGrid.add(createFieldLabel("Current Meter Reading: *"));
        txtCurrReading = createTextField(true);
        formGrid.add(txtCurrReading);

        // Row 3: Units Consumed
        formGrid.add(createFieldLabel("Units Consumed:"));
        txtUnitsConsumed = createTextField(false);
        txtUnitsConsumed.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtUnitsConsumed.setBackground(new Color(241, 245, 249));
        formGrid.add(txtUnitsConsumed);

        card.add(formGrid);
        return card;
    }

    private JPanel createBillDetailsCard() {
        JPanel card = createCardContainer("3. Bill Details");
        card.setPreferredSize(new Dimension(480, 410));

        JPanel formGrid = new JPanel(new GridLayout(7, 2, 8, 8));
        formGrid.setOpaque(false);

        // 1. Bill Number
        formGrid.add(createFieldLabel("Bill Number:"));
        txtBillNumber = createTextField(false);
        txtBillNumber.setText("Auto-generated");
        formGrid.add(txtBillNumber);

        // 2. Billing Period
        formGrid.add(createFieldLabel("Billing Period:"));
        YearMonth currentPeriod = YearMonth.now();
        cmbBillingPeriod = new JComboBox<>(List.of(
                currentPeriod.toString(),
                currentPeriod.minusMonths(1).toString(),
                currentPeriod.minusMonths(2).toString(),
                currentPeriod.minusMonths(3).toString()
        ).toArray(String[]::new));
        cmbBillingPeriod.setFont(UIConstants.FONT_INPUT);
        cmbBillingPeriod.setBackground(Color.WHITE);
        cmbBillingPeriod.setPreferredSize(new Dimension(160, 40));
        formGrid.add(cmbBillingPeriod);

        // 3. Tariff Category
        formGrid.add(createFieldLabel("Tariff:"));
        txtTariff = createTextField(false);
        formGrid.add(txtTariff);

        // 4. Energy Charge
        formGrid.add(createFieldLabel("Energy Charge:"));
        txtEnergyCharge = createTextField(false);
        formGrid.add(txtEnergyCharge);

        // 5. Fixed Charge
        formGrid.add(createFieldLabel("Fixed Charge:"));
        txtFixedCharge = createTextField(false);
        formGrid.add(txtFixedCharge);

        // 6. Tax / Other Charges
        formGrid.add(createFieldLabel("Tax/Other Charges:"));
        txtTaxCharge = createTextField(false);
        formGrid.add(txtTaxCharge);

        // 7. Total Bill Amount
        JLabel lblTotal = new JLabel("Total Bill Amount:");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(UIConstants.PRIMARY_NAVY);
        formGrid.add(lblTotal);

        txtTotalAmount = createTextField(false);
        txtTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTotalAmount.setForeground(UIConstants.PRIMARY_NAVY);
        txtTotalAmount.setBackground(new Color(239, 246, 255));
        formGrid.add(txtTotalAmount);

        card.add(formGrid);
        return card;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        btnCalculate = createButton("Calculate/Preview", UIConstants.ACCENT_BLUE);
        btnGenerateBill = createButton("Generate Bill", new Color(22, 163, 74)); // Green
        btnPrintPreview = createButton("Print/Preview", UIConstants.PRIMARY_NAVY);
        btnClear = createButton("Clear form", UIConstants.CLEAR_GRAY);

        bar.add(btnCalculate);
        bar.add(btnGenerateBill);
        bar.add(btnPrintPreview);
        bar.add(btnClear);

        return bar;
    }

    private JPanel createCardContainer(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        lblTitle.setForeground(UIConstants.PRIMARY_NAVY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BADGE);
        lbl.setForeground(UIConstants.TEXT_DARK);
        return lbl;
    }

    private JTextField createTextField(boolean editable) {
        JTextField field = new JTextField();
        UIConstants.styleTextField(field);
        field.setEditable(editable);
        field.setForeground(UIConstants.TEXT_DARK);
        field.setBackground(editable ? Color.WHITE : new Color(248, 250, 252));
        return field;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        UIConstants.styleButton(btn, bg, Color.WHITE);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 12, 34));
        return btn;
    }

    private void setupEvents() {
        btnSearchConsumer.addActionListener(e -> searchConsumer());
        btnCalculate.addActionListener(e -> calculateBillAction());
        btnGenerateBill.addActionListener(e -> generateBillAction());
        btnClear.addActionListener(e -> clearFields());
        btnPrintPreview.addActionListener(e -> printPreviewAction());

        // Pressing ENTER in Consumer ID triggers search
        txtConsumerId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchConsumer();
                }
            }
        });

        // Pressing ENTER in Current Reading triggers calculation
        txtCurrReading.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    calculateBillAction();
                }
            }
        });
    }

    /**
     * Searches MySQL for an active consumer and populates its current meter reading.
     */
    public void searchConsumer() {
        String consumerId = txtConsumerId.getText().trim();
        if (consumerId.isEmpty()) {
            showMessage(
                    "Consumer ID cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            txtConsumerId.requestFocusInWindow();
            return;
        }

        try {
            Optional<ConsumerSummary> opt = billService.findConsumerById(consumerId);
            if (opt.isPresent()) {
                this.currentConsumer = opt.get();
                txtConsumerName.setText(currentConsumer.fullName());
                txtAddress.setText(currentConsumer.address());
                txtMeterNumber.setText(currentConsumer.meterNumber());
                txtTariff.setText(currentConsumer.tariffCategory());
                txtPrevReading.setText(String.format(java.util.Locale.ROOT, "%.3f", currentConsumer.previousMeterReading()));
                lastCalculatedBill = null;
                billAlreadyGenerated = false;
                txtCurrReading.requestFocusInWindow();
            } else {
                this.currentConsumer = null;
                lastCalculatedBill = null;
                txtConsumerName.setText("");
                txtAddress.setText("");
                txtMeterNumber.setText("");
                txtPrevReading.setText("");
                showMessage("No active consumer was found with ID '" + consumerId + "'.",
                        "Consumer Not Found", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException exception) {
            showMessage(errorMessage(exception), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates inputs and computes bill line items.
     */
    public boolean calculateBillAction() {
        String consumerId = txtConsumerId.getText().trim();
        if (consumerId.isEmpty()) {
            showMessage("Consumer ID cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtConsumerId.requestFocusInWindow();
            return false;
        }

        String prevStr = txtPrevReading.getText().trim();
        if (prevStr.isEmpty()) {
            showMessage("Previous meter reading cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPrevReading.requestFocusInWindow();
            return false;
        }

        double prevReading;
        try {
            prevReading = Double.parseDouble(prevStr);
            if (prevReading < 0) {
                showMessage("Previous reading must be numeric and non-negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            showMessage("Previous reading must be numeric.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPrevReading.requestFocusInWindow();
            return false;
        }

        String currStr = txtCurrReading.getText().trim();
        if (currStr.isEmpty()) {
            showMessage("Current meter reading cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtCurrReading.requestFocusInWindow();
            return false;
        }

        double currReading;
        try {
            currReading = Double.parseDouble(currStr);
            if (currReading < 0) {
                showMessage("Current reading must be numeric and non-negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            showMessage("Current reading must be numeric.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtCurrReading.requestFocusInWindow();
            return false;
        }

        if (currReading < prevReading) {
            showMessage(
                    "Current reading cannot be less than previous reading.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            txtCurrReading.requestFocusInWindow();
            return false;
        }

        if (currentConsumer == null || !currentConsumer.consumerId().equalsIgnoreCase(consumerId)) {
            showMessage("Search for an active consumer before calculating a bill.", "Consumer Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String period = String.valueOf(cmbBillingPeriod.getSelectedItem());
        try {
            lastCalculatedBill = billService.calculateBill(currentConsumer, prevReading, currReading, period);
        } catch (RuntimeException exception) {
            lastCalculatedBill = null;
            showMessage(errorMessage(exception), "Bill Calculation Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Populate computed UI fields
        txtUnitsConsumed.setText(String.format(java.util.Locale.ROOT, "%.3f", lastCalculatedBill.unitsConsumed()));
        txtBillNumber.setText(lastCalculatedBill.billNumber());
        txtTariff.setText(lastCalculatedBill.tariffCategory());
        txtEnergyCharge.setText(lastCalculatedBill.formattedEnergyCharge());
        txtFixedCharge.setText(lastCalculatedBill.formattedFixedCharge());
        txtTaxCharge.setText(lastCalculatedBill.formattedTaxAmount());
        txtTotalAmount.setText(lastCalculatedBill.formattedTotalAmount());
        billAlreadyGenerated = false;

        return true;
    }

    /**
     * Finalizes and generates the electricity bill.
     */
    public void generateBillAction() {
        if (billAlreadyGenerated) {
            showMessage("This invoice has already been saved. Clear the form to start another bill.",
                    "Already Saved", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!calculateBillAction()) return;
        try {
            lastCalculatedBill = billService.generateBill(lastCalculatedBill);
            billAlreadyGenerated = true;
            txtBillNumber.setText(lastCalculatedBill.billNumber());
            showMessage("Bill saved to MySQL.\nBill Number: " + lastCalculatedBill.billNumber() +
                            "\nTotal Amount: " + lastCalculatedBill.formattedTotalAmount(),
                    "Bill Generation Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException exception) {
            showMessage(errorMessage(exception), "Bill Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens formatted electricity bill invoice preview dialog.
     */
    public void printPreviewAction() {
        if (lastCalculatedBill == null) {
            boolean ok = calculateBillAction();
            if (!ok) return;
        }

        if (!suppressDialogs && !GraphicsEnvironment.isHeadless()) {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            BillPreview dialog = new BillPreview(parentWindow, lastCalculatedBill);
            dialog.setVisible(true);
        }
    }

    /**
     * Resets all fields to their clean default states.
     */
    public void clearFields() {
        txtConsumerId.setText("");
        txtConsumerName.setText("");
        txtAddress.setText("");
        txtMeterNumber.setText("");
        txtPrevReading.setText("");
        txtCurrReading.setText("");
        txtUnitsConsumed.setText("");
        txtBillNumber.setText("Auto-generated");
        txtTariff.setText("");
        txtEnergyCharge.setText("");
        txtFixedCharge.setText("");
        txtTaxCharge.setText("");
        txtTotalAmount.setText("");
        cmbBillingPeriod.setSelectedIndex(0);

        currentConsumer = null;
        lastCalculatedBill = null;
        billAlreadyGenerated = false;
        txtConsumerId.requestFocusInWindow();
    }

    private String errorMessage(RuntimeException exception) {
        return exception.getMessage() == null ? "The billing operation could not be completed." : exception.getMessage();
    }

    // Accessors for automated tests
    public JTextField getTxtConsumerId() { return txtConsumerId; }
    public JTextField getTxtConsumerName() { return txtConsumerName; }
    public JTextField getTxtPrevReading() { return txtPrevReading; }
    public JTextField getTxtCurrReading() { return txtCurrReading; }
    public JTextField getTxtUnitsConsumed() { return txtUnitsConsumed; }
    public JTextField getTxtTotalAmount() { return txtTotalAmount; }
    public BillCalculationResult getLastCalculatedBill() { return lastCalculatedBill; }
}
