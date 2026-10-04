# Electricity Billing System

Java 21 Swing desktop application backed by MySQL through JDBC and Maven.

## Requirements

- JDK 21
- Apache Maven 3.9 or newer
- MySQL Server 8.0.16 or newer

## Database setup

1. Start the local MySQL Server service.
2. In MySQL Workbench, open and run `src/main/resources/database/schema.sql` to create the `electricity_billing` database and its sample tariff, consumer, user, reading, and bill data.
3. Set the application connection in the root `.env` file. Copy `.env.example` if needed, then set:

   ```text
   DB_URL=jdbc:mysql://localhost:3306/electricity_billing?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
   DB_USER=electricity_app
   DB_PASSWORD=your_local_database_account_password
   ```

   The local `create_app_user.sql` script is ignored by Git. Keep database passwords in `.env`; do not commit them.

## Build and run

Open PowerShell in the project directory:

```powershell
mvn clean compile
mvn test
mvn exec:java
```

The last command opens the login screen. MySQL must be running and `.env` must contain valid connection settings.

## Demo logins

The seed script includes these development accounts:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Admin | `manager` | `admin@2024` |
| Consumer | `user` | `user123` |

## Connected modules

- Login checks the MySQL user table, verifies BCrypt password hashes, enforces the stored role and account status, updates last login, and records activity.
- Admin consumer management searches, adds, edits, activates, and deactivates MySQL consumer records.
- Meter Reading and Bill Generation use active consumer, tariff, and tariff-slab data. Generating a bill saves the meter reading, bill, and audit activities in one transaction.
- Bill History searches saved bills by consumer or bill number and filters by billing period and payment status. It opens the saved bill preview.
- Admin Payments records full or partial received payments, checks the outstanding balance, settles a pending consumer request where present, updates bill status, and records activity.
- Reports loads consumer counts, billed and paid totals, outstanding balances, payment methods, and monthly revenue from MySQL.
- The consumer dashboard loads the signed-in consumer's profile and bill history. Consumers can update their address and contact details and save a local payment request.

Consumer payment requests remain `PENDING` until an administrator records the received payment. The application does not connect to a bank or payment gateway.

## Project layout

- `src/main/java/com/electricity/config` — database configuration and JDBC connection
- `src/main/java/com/electricity/auth` — login and authentication
- `src/main/java/com/electricity/admindashboard` — admin UI and JDBC-backed admin services
- `src/main/java/com/electricity/billgeneration` — meter readings, tariff calculations, and bill generation
- `src/main/java/com/electricity/userdashboard` — consumer profile, bill history, and payment request UI
- `src/main/resources/database/schema.sql` — schema and development seed data
