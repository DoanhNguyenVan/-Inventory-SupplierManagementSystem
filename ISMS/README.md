# 📦 ISMS — Inventory & Supplier Management System

> A full-stack web application for managing product inventory, supplier relationships, stock movements, and user access control. Built with **Spring Boot 3**, **Thymeleaf**, **SQL Server**, and **Bootstrap 5**.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [REST API Reference](#rest-api-reference)
- [User Roles and Permissions](#user-roles-and-permissions)
- [Business Rules](#business-rules)
- [Contributing](#contributing)

---

## Overview

ISMS (Inventory and Supplier Management System) is a multi-role web platform designed for small-to-medium businesses to:

- Track real-time product inventory levels with low-stock and out-of-stock alerts
- Manage supplier relationships linked to each product
- Record every stock-in / stock-out movement with full audit trails
- Safely soft-delete (discontinue) products with a Recycle Bin and one-click restore
- Suspend products temporarily (e.g., defective items) without removing them
- Control access via three distinct user roles: `ADMIN`, `STAFF`, and `USER`

---

## Features

### Product Management

| Feature | Description |
|---------|-------------|
| **Add / Edit Product** | Create new products with name and supplier assignment |
| **Soft Delete (Discontinue)** | Move products to Recycle Bin; stock warning shown if quantity > 0 |
| **Recycle Bin** | View all discontinued products; restore anytime |
| **Restore with Stock Edit** | On restore, set/update the stock quantity via a dedicated modal |
| **Suspend / Reactivate** | Toggle product status between `ACTIVE` and `SUSPENDED` for temporary issues |
| **Stock Status Badges** | Real-time badges: In Stock / Low Stock (<=5) / Out of Stock / Suspended / Discontinued |
| **Live Search** | Instant client-side filtering by name, supplier, or ID |
| **Tab Navigation** | Separate tabs for "Product List" and "Recycle Bin" with badge counters |

### Stock Movement Management

| Feature | Description |
|---------|-------------|
| **Stock In / Stock Out** | Record positive (in) or negative (out) quantity changes |
| **Stock Guard** | Blocks stock-out if quantity would go below 0 |
| **Suspended Product Block** | Stock movements (in or out) are blocked for `SUSPENDED` products |
| **Audit Trail** | Every movement is logged with the performing user and timestamp |
| **Movement History** | Full table of all movements with search |

### Supplier Management

| Feature | Description |
|---------|-------------|
| **CRUD Suppliers** | Add, edit, delete suppliers |
| **Supplier-Product Link** | Each product must be linked to exactly one supplier |
| **Safe Delete** | Suppliers with active products cannot be deleted |

### User Management (Admin only)

| Feature | Description |
|---------|-------------|
| **Register / Login** | Session-based authentication with password hashing |
| **Role Management** | Assign `ADMIN`, `STAFF`, or `USER` roles |
| **Account Control** | Enable / disable user accounts |

### Dashboard

- Summary cards: total products, active products, total suppliers, recent stock movements
- Quick navigation to all modules

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.4.3 |
| **Data Access** | Spring Data JPA, Hibernate 6 |
| **Database** | Microsoft SQL Server 2019+ |
| **Template Engine** | Thymeleaf 3 |
| **Frontend** | HTML5, Vanilla CSS, Vanilla JavaScript (ES2020+) |
| **UI Framework** | Bootstrap 5.3, Bootstrap Icons 1.11 |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build Tool** | Apache Maven |
| **Session Auth** | Spring MVC HttpSession |

---

## Project Structure

```
ISMS/
├── src/
│   └── main/
│       ├── java/com/Group5/ISMS/
│       │   ├── IsmsApplication.java          # Spring Boot entry point
│       │   ├── config/                        # Security and MVC config
│       │   ├── controller/
│       │   │   ├── AuthController.java        # Login, logout, register (MVC + API)
│       │   │   ├── DashboardController.java   # Dashboard page
│       │   │   ├── ProductController.java     # Product REST API
│       │   │   ├── StockMovementController.java
│       │   │   ├── SupplierController.java
│       │   │   └── UserController.java        # Admin user management
│       │   ├── dto/
│       │   │   ├── ApiResponse.java           # Generic JSON response wrapper
│       │   │   ├── ProductDto.java
│       │   │   ├── StockMovementDto.java
│       │   │   ├── SupplierDto.java
│       │   │   └── UserDto.java
│       │   ├── entity/
│       │   │   ├── Product.java               # Products table entity
│       │   │   ├── StockMovement.java         # Stock movements entity
│       │   │   ├── Supplier.java              # Suppliers entity
│       │   │   └── User.java                  # Users entity
│       │   ├── repository/
│       │   │   ├── ProductRepository.java     # JPA queries incl. soft-delete aware
│       │   │   ├── StockMovementRepository.java
│       │   │   ├── SupplierRepository.java
│       │   │   └── UserRepository.java
│       │   ├── service/
│       │   │   ├── ProductService.java        # Business logic: CRUD, soft-delete, restore
│       │   │   ├── StockMovementService.java  # Movement recording and stock validation
│       │   │   ├── SupplierService.java
│       │   │   └── UserService.java
│       │   ├── exception/                     # Global exception handling
│       │   └── util/                          # Password hashing and utilities
│       └── resources/
│           ├── application.properties         # App configuration
│           ├── templates/                     # Thymeleaf HTML pages
│           │   ├── login.html
│           │   ├── register.html
│           │   ├── index.html                 # Dashboard
│           │   ├── products.html              # Products + Recycle Bin
│           │   ├── stock.html                 # Stock Movements
│           │   ├── suppliers.html
│           │   └── users.html
│           └── static/
│               ├── css/style.css              # Custom styles
│               └── js/
│                   ├── products.js            # Products page logic
│                   ├── stock.js               # Stock movements logic
│                   ├── suppliers.js
│                   └── users.js
├── pom.xml
└── README.md
```

---

## Database Schema

### Table: `products`

| Column | Type | Description |
|--------|------|-------------|
| `product_id` | BIGINT (PK, Identity) | Unique product ID |
| `product_name` | VARCHAR(150) NOT NULL | Product name |
| `supplier_id` | BIGINT (FK) | Linked supplier |
| `quantity_in_stock` | INT DEFAULT 0 | Current inventory level |
| `status` | VARCHAR(30) DEFAULT 'ACTIVE' | `ACTIVE` / `SUSPENDED` / `DISCONTINUED` |
| `is_deleted` | BIT DEFAULT 0 | Soft-delete flag (1 = in Recycle Bin) |

### Table: `suppliers`

| Column | Type | Description |
|--------|------|-------------|
| `supplier_id` | BIGINT (PK, Identity) | Unique supplier ID |
| `supplier_name` | VARCHAR(150) NOT NULL | Supplier name |
| `contact_email` | VARCHAR(255) | Contact email |
| `phone` | VARCHAR(30) | Contact phone |
| `address` | VARCHAR(500) | Address |
| `is_deleted` | BIT DEFAULT 0 | Soft-delete flag |

### Table: `stock_movements`

| Column | Type | Description |
|--------|------|-------------|
| `movement_id` | BIGINT (PK, Identity) | Unique movement ID |
| `product_id` | BIGINT (FK) | Linked product |
| `user_id` | BIGINT (FK, nullable) | User who performed the movement |
| `quantity_change` | INT NOT NULL | Positive = Stock In, Negative = Stock Out |
| `movement_date` | DATETIME NOT NULL | Timestamp of the movement |
| `is_deleted` | BIT DEFAULT 0 | Soft-delete flag |

### Table: `users`

| Column | Type | Description |
|--------|------|-------------|
| `user_id` | BIGINT (PK, Identity) | Unique user ID |
| `username` | VARCHAR(100) UNIQUE NOT NULL | Login username |
| `password` | VARCHAR(255) NOT NULL | Hashed password |
| `full_name` | VARCHAR(200) | Display name |
| `email` | VARCHAR(255) | Email address |
| `role` | VARCHAR(20) DEFAULT 'USER' | `ADMIN` / `STAFF` / `USER` |
| `is_active` | BIT DEFAULT 1 | Account active flag |

---

## Getting Started

### Prerequisites

- **Java 17+** (JDK — not JRE)
- **Apache Maven 3.8+** (or use the included `mvnw` wrapper)
- **Microsoft SQL Server** (2019 or later recommended; Express edition works fine)
- **SQL Server Management Studio** (SSMS) or any SQL client

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/-Inventory-SupplierManagementSystem.git
cd -Inventory-SupplierManagementSystem/ISMS
```

### 2. Create the Database

Connect to your SQL Server instance and run:

```sql
CREATE DATABASE ISMS_DB;
```

> The application uses `spring.jpa.hibernate.ddl-auto=update`, so **all tables are created automatically** on first startup. You do **not** need to run any schema SQL manually.

### 3. Add the `status` Column (upgrade from older version only)

If you already have an existing `products` table without the `status` column, run this migration:

```sql
USE ISMS_DB;
ALTER TABLE products ADD status VARCHAR(30) DEFAULT 'ACTIVE';
UPDATE products SET status = 'ACTIVE' WHERE status IS NULL;
UPDATE products SET status = 'DISCONTINUED' WHERE is_deleted = 1;
```

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database — update with your SQL Server connection details
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ISMS_DB;encrypt=true;trustServerCertificate=true
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD_HERE

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.encoding=UTF-8

# Jackson — serialize LocalDateTime as ISO 8601 string
# This prevents "Invalid Date" from appearing in the browser
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss
```

---

## Running the Application

### Option A — Maven Wrapper (Recommended)

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

### Option B — Compile then Run JAR

```bash
.\mvnw.cmd package -DskipTests
java -jar target/ISMS-0.0.1-SNAPSHOT.jar
```

### Option C — From IDE (IntelliJ IDEA / VS Code)

Open `IsmsApplication.java` and run the `main` method directly.

---

Once started, open your browser and go to:

```
http://localhost:8080
```

You will be redirected to the **Login** page. Register the first account, then promote it to Admin directly in the database:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```

Interactive API documentation (Swagger UI) is available at:

```
http://localhost:8080/swagger-ui.html
```

---

## REST API Reference

All API endpoints are prefixed with `/api`. Authentication is **session-based** — you must be logged in via the web UI. All endpoints return a consistent JSON envelope:

**Success:**
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": [ ... ]
}
```

**Error:**
```json
{
  "success": false,
  "message": "Insufficient stock in warehouse! Product 'Mouse' currently only has 3 units.",
  "data": null
}
```

---

### Products API — `/api/products`

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/products` | Any | Get all active (non-deleted) products |
| `GET` | `/api/products/{id}` | Any | Get a single active product by ID |
| `GET` | `/api/products/trash` | ADMIN/STAFF | Get all discontinued products (Recycle Bin) |
| `GET` | `/api/products/counts` | Any | Get counts: `{ "active": N, "trash": N }` |
| `POST` | `/api/products` | ADMIN/STAFF | Create a new product |
| `PUT` | `/api/products/{id}` | ADMIN/STAFF | Update product name, supplier, or status |
| `DELETE` | `/api/products/{id}` | ADMIN/STAFF | Soft-delete (discontinue) a product |
| `PUT` | `/api/products/{id}/restore` | ADMIN/STAFF | Restore from Recycle Bin with new stock quantity |
| `PUT` | `/api/products/{id}/status` | ADMIN/STAFF | Switch between `ACTIVE` and `SUSPENDED` |

**Create / Update product body:**
```json
{
  "productName": "Wireless Mouse",
  "supplierId": 3,
  "status": "ACTIVE"
}
```

**Restore product body:**
```json
{
  "quantityInStock": 25
}
```

**Update status body:**
```json
{
  "status": "SUSPENDED"
}
```

---

### Stock Movements API — `/api/stock-movements`

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/stock-movements` | Any | Get all movements, ordered by date descending |
| `GET` | `/api/stock-movements/{id}` | Any | Get a single movement by ID |
| `POST` | `/api/stock-movements` | ADMIN/STAFF | Record a new stock movement |
| `DELETE` | `/api/stock-movements/{id}` | ADMIN/STAFF | Delete a movement (reverts the stock quantity) |

**Create movement body:**
```json
{
  "productId": 4,
  "quantityChange": 20,
  "movementDate": "2026-10-19T09:00:00"
}
```

> - Positive `quantityChange` = **Stock In** (increases inventory)
> - Negative `quantityChange` = **Stock Out** (decreases inventory)
> - Stock-out is blocked if the result would be negative
> - All stock movements are blocked for `SUSPENDED` products

---

### Suppliers API — `/api/suppliers`

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/suppliers` | Any | Get all active suppliers |
| `GET` | `/api/suppliers/{id}` | Any | Get a single supplier by ID |
| `POST` | `/api/suppliers` | ADMIN/STAFF | Create a new supplier |
| `PUT` | `/api/suppliers/{id}` | ADMIN/STAFF | Update supplier details |
| `DELETE` | `/api/suppliers/{id}` | ADMIN/STAFF | Soft-delete (blocked if has active products) |

---

### Users API — `/api/users` (Admin only)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/users` | ADMIN | Get all user accounts |
| `PUT` | `/api/users/{id}` | ADMIN | Update role or active status |
| `DELETE` | `/api/users/{id}` | ADMIN | Delete a user account |

---

## User Roles and Permissions

| Action | USER | STAFF | ADMIN |
|--------|:----:|:-----:|:-----:|
| View products | Yes | Yes | Yes |
| View stock movements | Yes | Yes | Yes |
| View suppliers | Yes | Yes | Yes |
| Add / Edit products | No | Yes | Yes |
| Discontinue products | No | Yes | Yes |
| Access Recycle Bin | No | Yes | Yes |
| Restore products | No | Yes | Yes |
| Suspend / Reactivate products | No | Yes | Yes |
| Record stock movements | No | Yes | Yes |
| Add / Edit suppliers | No | Yes | Yes |
| Manage user accounts | No | No | Yes |

---

## Business Rules

### Product Lifecycle

```
New Product
    |
    v
  ACTIVE  <------[Reactivate]-------+
    |                                |
    |--[Suspend]-->  SUSPENDED       |
    |               (no stock moves) |
    |                                |
    |--[Discontinue]--> is_deleted=1 (Recycle Bin) --[Restore]--> ACTIVE
```

### Soft Delete (Discontinue) Rules

- Any `ACTIVE` or `SUSPENDED` product can be discontinued regardless of remaining stock
- A **warning confirmation dialog** is shown if `quantity_in_stock > 0`
- Discontinued products are hidden from all normal product views and stock movement dropdowns
- A dedicated **Recycle Bin** tab (visible to ADMIN/STAFF only) shows all discontinued products

### Restore Rules

- Restoring a product opens a modal to **enter the new stock quantity** before confirming
- The product is set to `ACTIVE` status and `is_deleted = false` on restore

### Stock Movement Rules

- **Stock Out** is blocked if `currentStock + quantityChange < 0` (cannot go negative)
- **All stock movements** (both in and out) are blocked when a product is `SUSPENDED`
- Deleting a stock movement **reverts** the quantity change automatically (also blocked if it would cause negative stock)

### Supplier Rules

- Every product must be linked to exactly one supplier
- A supplier **cannot be deleted** if it has any active (non-deleted) products linked to it

---

## Contributing

1. Fork the repository
2. Create your feature branch

   ```bash
   git checkout -b feature/your-feature-name
   ```

3. Commit your changes

   ```bash
   git commit -m "Add: your feature description"
   ```

4. Push to your branch

   ```bash
   git push origin feature/your-feature-name
   ```

5. Open a Pull Request on GitHub

---

## License

This project was developed as part of a university group project (Group 5).  
All rights reserved.

---

*ISMS — Built with Spring Boot and Bootstrap*
