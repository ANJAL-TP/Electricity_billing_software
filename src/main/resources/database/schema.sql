-- Electricity Billing System development database
-- Requires MySQL 8.0.16+ so CHECK constraints are enforced.
-- Sample tariff amounts are illustrative INR values for local development.
-- Replace them with the rates approved for the project before real billing.

CREATE DATABASE IF NOT EXISTS electricity_billing
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE electricity_billing;

CREATE TABLE IF NOT EXISTS tariffs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tariff_code VARCHAR(20) NOT NULL,
    tariff_name VARCHAR(100) NOT NULL,
    fixed_charge DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_percentage DECIMAL(6, 3) NOT NULL DEFAULT 0.000,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tariffs_code (tariff_code),
    CONSTRAINT chk_tariffs_fixed_charge CHECK (fixed_charge >= 0),
    CONSTRAINT chk_tariffs_tax_percentage CHECK (tax_percentage >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS tariff_slabs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tariff_id BIGINT NOT NULL,
    min_units DECIMAL(12, 3) NOT NULL,
    max_units DECIMAL(12, 3) NULL,
    rate_per_unit DECIMAL(12, 4) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tariff_slabs_start (tariff_id, min_units),
    CONSTRAINT fk_tariff_slabs_tariff FOREIGN KEY (tariff_id)
        REFERENCES tariffs (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_tariff_slabs_min CHECK (min_units >= 0),
    CONSTRAINT chk_tariff_slabs_max CHECK (max_units IS NULL OR max_units > min_units),
    CONSTRAINT chk_tariff_slabs_rate CHECK (rate_per_unit >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS consumers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    consumer_id VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(150) NULL,
    meter_number VARCHAR(50) NOT NULL,
    tariff_category VARCHAR(20) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_consumers_consumer_id (consumer_id),
    UNIQUE KEY uq_consumers_meter_number (meter_number),
    KEY idx_consumers_status_name (status, name),
    KEY idx_consumers_phone (phone),
    CONSTRAINT fk_consumers_tariff FOREIGN KEY (tariff_category)
        REFERENCES tariffs (tariff_code) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    `role` ENUM('ADMIN', 'USER') NOT NULL,
    full_name VARCHAR(100) NULL,
    email VARCHAR(150) NULL,
    consumer_id VARCHAR(30) NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_consumer (consumer_id),
    KEY idx_users_role_status (`role`, status),
    CONSTRAINT fk_users_consumer FOREIGN KEY (consumer_id)
        REFERENCES consumers (consumer_id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS meter_readings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    consumer_id VARCHAR(30) NOT NULL,
    previous_reading DECIMAL(14, 3) NOT NULL,
    current_reading DECIMAL(14, 3) NOT NULL,
    units_consumed DECIMAL(14, 3) NOT NULL,
    reading_date DATE NOT NULL,
    billing_period CHAR(7) NOT NULL COMMENT 'Billing month in YYYY-MM format',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_meter_readings_period (consumer_id, billing_period),
    UNIQUE KEY uq_meter_readings_id_consumer (id, consumer_id),
    KEY idx_meter_readings_date (reading_date),
    CONSTRAINT fk_meter_readings_consumer FOREIGN KEY (consumer_id)
        REFERENCES consumers (consumer_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_meter_readings_nonnegative CHECK (
        previous_reading >= 0 AND current_reading >= previous_reading
    ),
    CONSTRAINT chk_meter_readings_units CHECK (
        units_consumed >= 0 AND units_consumed = current_reading - previous_reading
    )
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS bills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bill_number VARCHAR(40) NOT NULL,
    consumer_id VARCHAR(30) NOT NULL,
    meter_reading_id BIGINT NOT NULL,
    billing_period CHAR(7) NOT NULL COMMENT 'Billing month in YYYY-MM format',
    previous_reading DECIMAL(14, 3) NOT NULL,
    current_reading DECIMAL(14, 3) NOT NULL,
    units_consumed DECIMAL(14, 3) NOT NULL,
    tariff_category VARCHAR(20) NOT NULL,
    energy_charge DECIMAL(12, 2) NOT NULL,
    fixed_charge DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    tax_charge DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    other_charge DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL,
    payment_status ENUM('UNPAID', 'PAID', 'PARTIALLY_PAID', 'OVERDUE') NOT NULL DEFAULT 'UNPAID',
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_bills_number (bill_number),
    UNIQUE KEY uq_bills_reading (meter_reading_id),
    UNIQUE KEY uq_bills_consumer_period (consumer_id, billing_period),
    UNIQUE KEY uq_bills_id_consumer (id, consumer_id),
    KEY idx_bills_consumer_generated (consumer_id, generated_at),
    KEY idx_bills_payment_status (payment_status),
    CONSTRAINT fk_bills_consumer FOREIGN KEY (consumer_id)
        REFERENCES consumers (consumer_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bills_meter_reading_consumer FOREIGN KEY (meter_reading_id, consumer_id)
        REFERENCES meter_readings (id, consumer_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bills_tariff FOREIGN KEY (tariff_category)
        REFERENCES tariffs (tariff_code) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bills_readings CHECK (
        previous_reading >= 0
        AND current_reading >= previous_reading
        AND units_consumed = current_reading - previous_reading
    ),
    CONSTRAINT chk_bills_charges CHECK (
        energy_charge >= 0 AND fixed_charge >= 0 AND tax_charge >= 0
        AND other_charge >= 0 AND total_amount >= 0
        AND total_amount = energy_charge + fixed_charge + tax_charge + other_charge
    )
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bill_id BIGINT NOT NULL,
    consumer_id VARCHAR(30) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method ENUM('CASH', 'UPI', 'CARD', 'BANK_TRANSFER') NOT NULL,
    transaction_reference VARCHAR(100) NULL,
    payment_date DATETIME NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payments_transaction_reference (transaction_reference),
    KEY idx_payments_bill_date (bill_id, payment_date),
    KEY idx_payments_consumer_date (consumer_id, payment_date),
    CONSTRAINT fk_payments_bill_consumer FOREIGN KEY (bill_id, consumer_id)
        REFERENCES bills (id, consumer_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_payments_amount CHECK (amount > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    activity_type ENUM(
        'LOGIN',
        'CONSUMER_CREATED',
        'CONSUMER_UPDATED',
        'CONSUMER_DEACTIVATED',
        'METER_READING_ADDED',
        'BILL_GENERATED',
        'PAYMENT_RECEIVED',
        'PROFILE_UPDATED'
    ) NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_activities_created_at (created_at),
    KEY idx_activities_user_created (user_id, created_at),
    CONSTRAINT fk_activities_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB;

-- Development tariff examples, expressed in INR. Each slab's min_units is
-- the previous threshold; max_units is the inclusive upper threshold. NULL
-- max_units means the final slab has no upper limit.
INSERT IGNORE INTO tariffs (tariff_code, tariff_name, fixed_charge, tax_percentage, active) VALUES
    ('LT-1', 'Domestic', 50.00, 5.000, TRUE),
    ('LT-2', 'Commercial', 150.00, 10.000, TRUE),
    ('HT', 'Industrial', 500.00, 15.000, TRUE);

INSERT IGNORE INTO tariff_slabs (tariff_id, min_units, max_units, rate_per_unit)
SELECT t.id, s.min_units, s.max_units, s.rate_per_unit
FROM tariffs AS t
JOIN (
    SELECT 'LT-1' AS tariff_code, 0.000 AS min_units, 50.000 AS max_units, 2.0000 AS rate_per_unit UNION ALL
    SELECT 'LT-1', 50.000, 100.000, 3.0000 UNION ALL
    SELECT 'LT-1', 100.000, 200.000, 4.5000 UNION ALL
    SELECT 'LT-1', 200.000, 300.000, 5.5000 UNION ALL
    SELECT 'LT-1', 300.000, NULL, 6.5000 UNION ALL
    SELECT 'LT-2', 0.000, 50.000, 6.0000 UNION ALL
    SELECT 'LT-2', 50.000, 100.000, 7.0000 UNION ALL
    SELECT 'LT-2', 100.000, 200.000, 8.0000 UNION ALL
    SELECT 'LT-2', 200.000, 300.000, 9.0000 UNION ALL
    SELECT 'LT-2', 300.000, NULL, 10.0000 UNION ALL
    SELECT 'HT', 0.000, 50.000, 8.0000 UNION ALL
    SELECT 'HT', 50.000, 100.000, 9.0000 UNION ALL
    SELECT 'HT', 100.000, 200.000, 10.0000 UNION ALL
    SELECT 'HT', 200.000, 300.000, 11.0000 UNION ALL
    SELECT 'HT', 300.000, NULL, 12.0000
) AS s ON s.tariff_code = t.tariff_code;

-- Demo credentials: admin/admin123, manager/admin@2024, user/user123.
-- Password hashes use BCrypt ($2b$, cost 12); no plain-text passwords are stored.
INSERT IGNORE INTO users (username, password_hash, `role`, full_name, email, consumer_id, status) VALUES
    ('admin', '$2b$12$D/QGoL2ZOU0Qslk36mXBDOHnbLroYdNibmfZTSuNPJjDyA9A3.OoG', 'ADMIN', 'System Administrator', 'admin@ebs.com', NULL, 'ACTIVE'),
    ('manager', '$2b$12$zcjuSSGlyQkTgnhgRcC9ke6iKgL1k.GbZUSpUD0D10WAi1Lf0Eul.', 'ADMIN', 'Billing Manager', 'manager@ebs.com', NULL, 'ACTIVE');

INSERT IGNORE INTO consumers
    (consumer_id, name, address, phone, email, meter_number, tariff_category, status)
VALUES
    ('EBS-1001', 'Rajesh Kumar', '142 Green Avenue, Phase 2, North District', '9876543210', 'rajesh.kumar@example.com', 'MTR-88102', 'LT-1', 'ACTIVE'),
    ('EBS-1002', 'Anita Sharma', 'Flat 4B, Skyline Heights, Civil Lines', '9876501234', 'anita.sharma@example.com', 'MTR-99201', 'LT-1', 'ACTIVE'),
    ('EBS-2001', 'Metro Mart & Retail', 'Plot 28, Commercial Zone, Sector 18', '9567890123', 'accounts@metromart.example', 'MTR-77150', 'LT-2', 'ACTIVE'),
    ('EBS-3001', 'Apex Engineering Ltd', 'Phase 1 Industrial Corridor, Plot 502', '9447701122', 'billing@apexengineering.example', 'MTR-55420', 'HT', 'ACTIVE');

INSERT IGNORE INTO users (username, password_hash, `role`, full_name, email, consumer_id, status) VALUES
    ('user', '$2b$12$f8sYKbAmP/7QCb0K3B4zNuVjcqi70J22RlrtmzJ.daSrexGyzrEL.', 'USER', 'Rajesh Kumar', 'rajesh.kumar@example.com', 'EBS-1001', 'ACTIVE');

-- Starting meter snapshots for the August 2026 demo billing period.
INSERT IGNORE INTO meter_readings
    (consumer_id, previous_reading, current_reading, units_consumed, reading_date, billing_period)
VALUES
    ('EBS-1001', 4440.000, 4520.000, 80.000, '2026-08-31', '2026-08'),
    ('EBS-1002', 3000.000, 3100.000, 100.000, '2026-08-31', '2026-08'),
    ('EBS-2001', 12250.000, 12400.000, 150.000, '2026-08-31', '2026-08'),
    ('EBS-3001', 58700.000, 58900.000, 200.000, '2026-08-31', '2026-08');

-- Sample bills use the development rates above and can be removed for a clean demo database.
INSERT IGNORE INTO bills
    (bill_number, consumer_id, meter_reading_id, billing_period, previous_reading,
     current_reading, units_consumed, tariff_category, energy_charge, fixed_charge,
     tax_charge, other_charge, total_amount, payment_status, generated_at, due_date)
SELECT 'EB-2026-000001', mr.consumer_id, mr.id, mr.billing_period, mr.previous_reading,
       mr.current_reading, mr.units_consumed, c.tariff_category, 190.00, 50.00,
       9.50, 0.00, 249.50, 'PAID', '2026-09-01 09:00:00', '2026-09-30'
FROM meter_readings AS mr JOIN consumers AS c ON c.consumer_id = mr.consumer_id
WHERE mr.consumer_id = 'EBS-1001' AND mr.billing_period = '2026-08';

INSERT IGNORE INTO bills
    (bill_number, consumer_id, meter_reading_id, billing_period, previous_reading,
     current_reading, units_consumed, tariff_category, energy_charge, fixed_charge,
     tax_charge, other_charge, total_amount, payment_status, generated_at, due_date)
SELECT 'EB-2026-000002', mr.consumer_id, mr.id, mr.billing_period, mr.previous_reading,
       mr.current_reading, mr.units_consumed, c.tariff_category, 250.00, 50.00,
       12.50, 0.00, 312.50, 'UNPAID', '2026-09-01 09:10:00', '2026-09-30'
FROM meter_readings AS mr JOIN consumers AS c ON c.consumer_id = mr.consumer_id
WHERE mr.consumer_id = 'EBS-1002' AND mr.billing_period = '2026-08';

INSERT IGNORE INTO bills
    (bill_number, consumer_id, meter_reading_id, billing_period, previous_reading,
     current_reading, units_consumed, tariff_category, energy_charge, fixed_charge,
     tax_charge, other_charge, total_amount, payment_status, generated_at, due_date)
SELECT 'EB-2026-000003', mr.consumer_id, mr.id, mr.billing_period, mr.previous_reading,
       mr.current_reading, mr.units_consumed, c.tariff_category, 1050.00, 150.00,
       105.00, 0.00, 1305.00, 'UNPAID', '2026-09-01 09:20:00', '2026-09-30'
FROM meter_readings AS mr JOIN consumers AS c ON c.consumer_id = mr.consumer_id
WHERE mr.consumer_id = 'EBS-2001' AND mr.billing_period = '2026-08';

INSERT IGNORE INTO bills
    (bill_number, consumer_id, meter_reading_id, billing_period, previous_reading,
     current_reading, units_consumed, tariff_category, energy_charge, fixed_charge,
     tax_charge, other_charge, total_amount, payment_status, generated_at, due_date)
SELECT 'EB-2026-000004', mr.consumer_id, mr.id, mr.billing_period, mr.previous_reading,
       mr.current_reading, mr.units_consumed, c.tariff_category, 1850.00, 500.00,
       277.50, 0.00, 2627.50, 'UNPAID', '2026-09-01 09:30:00', '2026-09-30'
FROM meter_readings AS mr JOIN consumers AS c ON c.consumer_id = mr.consumer_id
WHERE mr.consumer_id = 'EBS-3001' AND mr.billing_period = '2026-08';

INSERT IGNORE INTO payments
    (bill_id, consumer_id, amount, payment_method, transaction_reference, payment_date, status)
SELECT b.id, b.consumer_id, b.total_amount, 'UPI', 'DEMO-UPI-EB-2026-000001',
       '2026-09-03 10:15:00', 'SUCCESS'
FROM bills AS b WHERE b.bill_number = 'EB-2026-000001';

INSERT INTO activities (user_id, activity_type, description, created_at)
SELECT u.id, 'BILL_GENERATED', 'Demo bill EB-2026-000001 generated for EBS-1001.', '2026-09-01 09:00:00'
FROM users AS u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM activities AS a
      WHERE a.activity_type = 'BILL_GENERATED'
        AND a.description = 'Demo bill EB-2026-000001 generated for EBS-1001.'
  );

INSERT INTO activities (user_id, activity_type, description, created_at)
SELECT u.id, 'PAYMENT_RECEIVED', 'Demo payment received for bill EB-2026-000001.', '2026-09-03 10:15:00'
FROM users AS u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM activities AS a
      WHERE a.activity_type = 'PAYMENT_RECEIVED'
        AND a.description = 'Demo payment received for bill EB-2026-000001.'
  );
