-- ==========================================================
-- Inventory & Supplier Management System (ISMS)
-- Database: Microsoft SQL Server (ISMS_DB)
-- ==========================================================

USE ISMS_DB;
GO

-- 1. Table: users (Authentication & Authorization)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(100),
        email NVARCHAR(100) UNIQUE,
        role NVARCHAR(20) DEFAULT 'USER',
        created_at DATETIME DEFAULT GETDATE()
    );
END
GO

-- 2. Table: suppliers (One supplier can supply many products)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'suppliers')
BEGIN
    CREATE TABLE suppliers (
        supplier_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        supplier_name NVARCHAR(100) NOT NULL,
        email NVARCHAR(100),
        phone NVARCHAR(20)
    );
END
GO

-- 3. Table: products (One product can have many stock movements)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
    CREATE TABLE products (
        product_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        supplier_id BIGINT,
        product_name NVARCHAR(150) NOT NULL,
        quantity_in_stock INT DEFAULT 0,
        CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) 
            REFERENCES suppliers(supplier_id) ON DELETE SET NULL
    );
END
GO

-- 4. Table: stock_movements (Tracks inventory in/out)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_movements')
BEGIN
    CREATE TABLE stock_movements (
        movement_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        product_id BIGINT NOT NULL,
        quantity_change INT NOT NULL,
        movement_date DATETIME DEFAULT GETDATE(),
        CONSTRAINT fk_movements_product FOREIGN KEY (product_id) 
            REFERENCES products(product_id) ON DELETE CASCADE
    );
END
GO
