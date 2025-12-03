
-----

# 🏭 Pandakasalai (Inventory & Logistics ERP)

**Pandakasalai** (Tamil: பண்டகசாலை, meaning *Warehouse*) is a high-performance, concurrency-safe Inventory & Logistics microservice built as part of a modular ERP system.

This service is designed to handle high-velocity stock reservations and high-performance lookups using advanced architectural patterns like **Optimistic Locking**, **Transactional Event Listeners**, and **In-Memory Caching**.

## 🚀 Key Features

* **🛡️ Concurrency Control:** Implements **Optimistic Locking** using JPA `@Version` to prevent race conditions (Double Booking) during high-volume concurrent reservations.
* **⚡ High-Performance Search:** Uses a decoupled **In-Memory Cache (`ConcurrentHashMap`)** for $O(1)$ read performance, avoiding database hits for frequent lookups.
* **🔄 Eventual Consistency:** Decouples cache synchronization from business logic using Spring's **`ApplicationEventPublisher`** and **`@TransactionalEventListener(phase = AFTER_COMMIT)`**, ensuring the cache is only updated after a successful database commit.
* **⚠️ Robust Error Handling:** Features a **Global Exception Handler** (`@RestControllerAdvice`) that maps internal concurrency failures to proper **HTTP 409 Conflict** responses.
* **🧪 Comprehensive Testing:** Fully validated with **Unit Tests** (Mockito) and **Integration Tests** (Spring Boot Test) covering concurrency scenarios.

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Database:** PostgreSQL
* **Build Tool:** Maven
* **Testing:** JUnit 5, Mockito, Bruno (CLI)

## 📂 Project Structure

```bash
src/main/java/com/vanikathunaivan/pandakasalai/
├── controller/       # REST Controllers (API Layer)
├── service/          # Business Logic & Transaction Management
├── repository/       # JPA Repositories
├── model/            # JPA Entities (@Version enabled)
├── dto/              # Data Transfer Objects
├── events/           # Custom Spring Events (InventoryUpdatedEvent)
└── exceptions/       # Custom Exceptions & Global Handlers
```

## ⚙️ Setup & Installation

### 1\. Prerequisites

* Java 21 SDK
* PostgreSQL Database
* Maven

### 2\. Database Setup

Create the database and the required table. You can use the `psql` CLI or a tool like pgAdmin.

```sql
CREATE DATABASE inventory_db;

-- Table Schema
CREATE TABLE bin_inventory (
    id BIGSERIAL PRIMARY KEY,
    sku_id VARCHAR(50) NOT NULL,
    warehouse_id INT NOT NULL,
    bin_code VARCHAR(20) NOT NULL,
    available_qty INT NOT NULL CHECK (available_qty >= 0),
    reserved_qty INT DEFAULT 0 CHECK (reserved_qty >= 0),
    version BIGINT NOT NULL DEFAULT 0, -- Critical for Optimistic Locking
    UNIQUE (sku_id, warehouse_id, bin_code)
);
```

### 3\. Bulk Data Seeding (Optional)

The repository includes `inventory_data_fixed.csv` for bulk loading 1 million records for performance testing.

```sql
-- Run this in psql terminal
\copy bin_inventory (sku_id, warehouse_id, bin_code, available_qty) FROM './inventory_data_fixed.csv' DELIMITER ',' CSV HEADER;
```

### 4\. Build and Run

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

## 🔌 API Endpoints

### 1\. Reserve Stock

Attempts to reserve stock. Returns `200 OK` on success or `409 Conflict` if the stock was modified by another transaction.

* **URL:** `POST /api/inventory/reserve`
* **Body:**
  ```json
  {
    "skuId": "SKU-658",
    "quantity": 10,
    "warehouseId": 1,
    "binCode": "W1-B1"
  }
  ```
* **Responses:**
    * `200 OK`: Reservation Successful.
    * `409 Conflict`: Concurrency Error (Please Retry).
    * `400 Bad Request`: Insufficient Inventory.

## 🧪 Testing Concurrency

To verify the Optimistic Locking mechanism, use the **Bruno API Client** CLI tool to send parallel requests.

1.  **Reset Test Data:**
    ```sql
    UPDATE bin_inventory SET available_qty=10, version=1 WHERE sku_id='SKU-658' AND bin_code='W1-B1';
    ```
2.  **Run Bruno Test:**
    ```bash
    bru run "Reserve_Stock_Clash_Test.bru" --iteration-count 10 --parallel
    ```
3.  **Expected Result:**
    * **1 Request** returns `200 OK`.
    * **9 Requests** return `409 Conflict`.

## 📜 License

This project is part of a learning series on building complex, modular ERP architectures.