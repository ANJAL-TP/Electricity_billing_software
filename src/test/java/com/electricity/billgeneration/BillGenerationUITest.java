package com.electricity.billgeneration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class BillGenerationUITest {

    private BillGeneration billWindow;
    private BillGenerationPanel panel;

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            billWindow = new BillGeneration();
            panel = billWindow.getBillPanel();
            panel.setSuppressDialogs(true);
        });
    }

    @Test
    @DisplayName("Should initialize Bill Generation components properly")
    void testComponentInitialization() {
        assertNotNull(billWindow);
        assertNotNull(panel);
        assertNotNull(panel.getTxtConsumerId());
        assertNotNull(panel.getTxtConsumerName());
        assertNotNull(panel.getTxtPrevReading());
        assertNotNull(panel.getTxtCurrReading());
        assertNotNull(panel.getTxtUnitsConsumed());
        assertNotNull(panel.getTxtTotalAmount());
    }

    @Test
    @DisplayName("Searching existing consumer should populate form fields")
    void testSearchConsumerAutoFill() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel.getTxtConsumerId().setText("EBS-1001");
            panel.searchConsumer();

            assertEquals("Rajesh Kumar", panel.getTxtConsumerName().getText());
            assertEquals("4520.000", panel.getTxtPrevReading().getText());
        });
    }

    @Test
    @DisplayName("Clear should reset all input and calculated fields")
    void testClearFields() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel.getTxtConsumerId().setText("EBS-1001");
            panel.searchConsumer();
            panel.getTxtCurrReading().setText("4800");

            panel.calculateBillAction();
            assertNotNull(panel.getLastCalculatedBill());

            // Trigger Clear
            panel.clearFields();

            assertEquals("", panel.getTxtConsumerId().getText());
            assertEquals("", panel.getTxtConsumerName().getText());
            assertEquals("", panel.getTxtPrevReading().getText());
            assertEquals("", panel.getTxtCurrReading().getText());
            assertEquals("", panel.getTxtUnitsConsumed().getText());
            assertEquals("", panel.getTxtTotalAmount().getText());
            assertNull(panel.getLastCalculatedBill());
        });
    }

    @Test
    @DisplayName("Validation fails when Current Reading is less than Previous Reading")
    void testInvalidReadingRange() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel.getTxtConsumerId().setText("EBS-1001");
            panel.getTxtPrevReading().setText("5000");
            panel.getTxtCurrReading().setText("4800"); // Current < Previous

            boolean result = panel.calculateBillAction();
            assertFalse(result, "Calculation should fail when current < previous");
            assertNull(panel.getLastCalculatedBill());
        });
    }

    @Test
    @DisplayName("Validation fails when Consumer ID is empty")
    void testEmptyConsumerId() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel.getTxtConsumerId().setText("");
            panel.getTxtPrevReading().setText("100");
            panel.getTxtCurrReading().setText("200");

            boolean result = panel.calculateBillAction();
            assertFalse(result);
        });
    }

    @Test
    @DisplayName("Successful calculation populates units and total amount")
    void testSuccessfulCalculation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel.getTxtConsumerId().setText("EBS-1001");
            panel.searchConsumer();
            panel.getTxtPrevReading().setText("4520");
            panel.getTxtCurrReading().setText("4840");

            boolean result = panel.calculateBillAction();
            assertTrue(result);
            assertEquals("320.000", panel.getTxtUnitsConsumed().getText());
            assertNotNull(panel.getLastCalculatedBill());
            assertTrue(panel.getTxtTotalAmount().getText().length() > 0);
        });
    }
}
