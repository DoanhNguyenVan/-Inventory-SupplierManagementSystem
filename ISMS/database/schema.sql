IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ISMS_DB')
BEGIN
    CREATE DATABASE ISMS_DB;
END
GO

USE ISMS_DB;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        user_id    BIGINT IDENTITY(1,1) PRIMARY KEY,
        username   NVARCHAR(50)  NOT NULL UNIQUE,
        password   NVARCHAR(255) NOT NULL,
        full_name  NVARCHAR(100),
        email      NVARCHAR(100) UNIQUE,
        role       NVARCHAR(20)  DEFAULT 'USER' NOT NULL,
        is_deleted BIT           DEFAULT 0 NOT NULL,
        created_at DATETIME      DEFAULT GETDATE()
    );
END
ELSE
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'is_deleted')
    BEGIN
        ALTER TABLE users ADD is_deleted BIT DEFAULT 0 NOT NULL;
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'created_at')
    BEGIN
        ALTER TABLE users ADD created_at DATETIME DEFAULT GETDATE();
    END
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'suppliers')
BEGIN
    CREATE TABLE suppliers (
        supplier_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
        supplier_name NVARCHAR(100) NOT NULL,
        email         NVARCHAR(100),
        phone         NVARCHAR(20),
        is_deleted    BIT DEFAULT 0 NOT NULL
    );
END
ELSE
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('suppliers') AND name = 'is_deleted')
    BEGIN
        ALTER TABLE suppliers ADD is_deleted BIT DEFAULT 0 NOT NULL;
    END
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
    CREATE TABLE products (
        product_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
        supplier_id       BIGINT,
        product_name      NVARCHAR(150) NOT NULL,
        quantity_in_stock INT DEFAULT 0 NOT NULL,
        status            NVARCHAR(30) DEFAULT 'ACTIVE' NOT NULL,
        is_deleted        BIT DEFAULT 0 NOT NULL,

        CONSTRAINT fk_products_supplier 
            FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) 
            ON DELETE SET NULL
    );
END
ELSE
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('products') AND name = 'is_deleted')
    BEGIN
        ALTER TABLE products ADD is_deleted BIT DEFAULT 0 NOT NULL;
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('products') AND name = 'status')
    BEGIN
        ALTER TABLE products ADD status NVARCHAR(30) DEFAULT 'ACTIVE' NOT NULL;
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'fk_products_supplier')
    BEGIN
        ALTER TABLE products ADD CONSTRAINT fk_products_supplier 
            FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) 
            ON DELETE SET NULL;
    END
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_movements')
BEGIN
    CREATE TABLE stock_movements (
        movement_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
        product_id      BIGINT NOT NULL,
        user_id         BIGINT,
        quantity_change INT NOT NULL,
        movement_date   DATETIME DEFAULT GETDATE() NOT NULL,
        is_deleted      BIT DEFAULT 0 NOT NULL,

        CONSTRAINT fk_movements_product 
            FOREIGN KEY (product_id) REFERENCES products(product_id) 
            ON DELETE CASCADE,

        CONSTRAINT fk_movements_user 
            FOREIGN KEY (user_id) REFERENCES users(user_id) 
            ON DELETE SET NULL
    );
END
ELSE
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('stock_movements') AND name = 'user_id')
    BEGIN
        ALTER TABLE stock_movements ADD user_id BIGINT;
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('stock_movements') AND name = 'is_deleted')
    BEGIN
        ALTER TABLE stock_movements ADD is_deleted BIT DEFAULT 0 NOT NULL;
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'fk_movements_product')
    BEGIN
        ALTER TABLE stock_movements ADD CONSTRAINT fk_movements_product 
            FOREIGN KEY (product_id) REFERENCES products(product_id) 
            ON DELETE CASCADE;
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'fk_movements_user')
    BEGIN
        ALTER TABLE stock_movements ADD CONSTRAINT fk_movements_user 
            FOREIGN KEY (user_id) REFERENCES users(user_id) 
            ON DELETE SET NULL;
    END
END
GO
