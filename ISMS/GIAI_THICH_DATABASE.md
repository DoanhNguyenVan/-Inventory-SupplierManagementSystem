# GIẢI THÍCH CƠ SỞ DỮ LIỆU
## HỆ THỐNG QUẢN LÝ TỒN KHO & NHÀ CUNG CẤP (ISMS)

---

## 1. THÔNG TIN CHUNG

| Thuộc tính | Giá trị |
|-----------|---------|
| Hệ quản trị CSDL | Microsoft SQL Server 2016+ |
| Tên database | `ISMS_DB` |
| Encoding | Unicode (NVARCHAR cho tiếng Việt) |
| Số bảng | 4 |
| Cơ chế xóa | Soft Delete (is_deleted BIT) |

---

## 2. THIẾT KẾ TỪNG BẢNG

### 2.1. Bảng `users` — Tài khoản người dùng

```sql
CREATE TABLE users (
    user_id    BIGINT IDENTITY(1,1) PRIMARY KEY,
    username   NVARCHAR(50)  NOT NULL UNIQUE,
    password   NVARCHAR(255) NOT NULL,
    full_name  NVARCHAR(100),
    email      NVARCHAR(100) UNIQUE,
    role       NVARCHAR(20)  DEFAULT 'USER',
    is_deleted BIT           DEFAULT 0 NOT NULL,
    created_at DATETIME      DEFAULT GETDATE()
);
```

| Cột | Kiểu dữ liệu | Ràng buộc | Mục đích |
|-----|-------------|-----------|---------|
| `user_id` | BIGINT | PK, AUTO_INCREMENT | Định danh duy nhất |
| `username` | NVARCHAR(50) | NOT NULL, UNIQUE | Tên đăng nhập |
| `password` | NVARCHAR(255) | NOT NULL | Mật khẩu đã hash (PBKDF2) |
| `full_name` | NVARCHAR(100) | Nullable | Tên hiển thị |
| `email` | NVARCHAR(100) | UNIQUE, Nullable | Email đăng nhập thay thế |
| `role` | NVARCHAR(20) | DEFAULT 'USER' | Phân quyền: `ADMIN` / `STAFF` / `USER` |
| `is_deleted` | BIT | DEFAULT 0, NOT NULL | Soft Delete flag |
| `created_at` | DATETIME | DEFAULT GETDATE() | Thời điểm tạo tài khoản |

**Ghi chú thiết kế:**
- Cột `password` lưu chuỗi hash dạng `Base64(salt):Base64(hash)`, không lưu plaintext.
- `email` có thể NULL vì đăng ký chỉ bắt buộc username.
- `role` có 3 giá trị hợp lệ: `ADMIN`, `STAFF`, `USER` — kiểm soát ở tầng Service.
- Khi `is_deleted = 1`, tài khoản không thể đăng nhập dù thông tin còn trong DB.

---

### 2.2. Bảng `suppliers` — Nhà cung cấp

```sql
CREATE TABLE suppliers (
    supplier_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    supplier_name NVARCHAR(100) NOT NULL,
    email         NVARCHAR(100),
    phone         NVARCHAR(20),
    is_deleted    BIT DEFAULT 0 NOT NULL
);
```

| Cột | Kiểu dữ liệu | Ràng buộc | Mục đích |
|-----|-------------|-----------|---------|
| `supplier_id` | BIGINT | PK, AUTO_INCREMENT | Định danh duy nhất |
| `supplier_name` | NVARCHAR(100) | NOT NULL | Tên nhà cung cấp |
| `email` | NVARCHAR(100) | Nullable | Email liên hệ |
| `phone` | NVARCHAR(20) | Nullable | Số điện thoại |
| `is_deleted` | BIT | DEFAULT 0, NOT NULL | Soft Delete flag |

**Ghi chú thiết kế:**
- Không có UNIQUE trên `supplier_name` — cho phép nhiều nhà cung cấp trùng tên (thực tế có thể có chi nhánh).
- `phone` dùng NVARCHAR thay vì INT/BIGINT để lưu được số có dấu `+`, dấu `-`, khoảng trắng (ví dụ: `+84 901 234 567`).

---

### 2.3. Bảng `products` — Sản phẩm

```sql
CREATE TABLE products (
    product_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    supplier_id      BIGINT,
    product_name     NVARCHAR(150) NOT NULL,
    quantity_in_stock INT DEFAULT 0,
    status           NVARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    is_deleted       BIT DEFAULT 0 NOT NULL,

    CONSTRAINT fk_products_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL
);
```

| Cột | Kiểu dữ liệu | Ràng buộc | Mục đích |
|-----|-------------|-----------|---------|
| `product_id` | BIGINT | PK, AUTO_INCREMENT | Định danh duy nhất |
| `supplier_id` | BIGINT | FK → suppliers, Nullable | Nhà cung cấp của sản phẩm |
| `product_name` | NVARCHAR(150) | NOT NULL | Tên sản phẩm |
| `quantity_in_stock` | INT | DEFAULT 0 | Số lượng tồn kho hiện tại |
| `status` | NVARCHAR(20) | DEFAULT 'ACTIVE', NOT NULL | Trạng thái kinh doanh: `ACTIVE` (đang bán) / `SUSPENDED` (tạm ngưng) |
| `is_deleted` | BIT | DEFAULT 0, NOT NULL | Soft Delete flag (1 = trong Thùng rác / Discontinued) |

**Ghi chú thiết kế:**
- `status`: Quản lý vòng đời sản phẩm. Khi `status = 'SUSPENDED'`, sản phẩm tạm ngưng kinh doanh, mọi giao dịch xuất/nhập kho liên quan đều bị chặn ở tầng Service.
- `supplier_id` Nullable + `ON DELETE SET NULL`: nếu nhà cung cấp bị xóa, sản phẩm vẫn tồn tại, trường `supplier_id` trở thành NULL.
- `quantity_in_stock` là **trường đồng bộ** (denormalized): giá trị bằng tổng đại số tất cả `quantity_change` trong `stock_movements`. Cập nhật mỗi khi có phiếu kho mới hoặc khi khôi phục sản phẩm từ Thùng rác.
- Không có ràng buộc `CHECK (quantity_in_stock >= 0)` ở cấp database — kiểm tra ở tầng Service để có thông báo lỗi rõ ràng.

---

### 2.4. Bảng `stock_movements` — Phiếu biến động kho

```sql
CREATE TABLE stock_movements (
    movement_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id      BIGINT   NOT NULL,
    user_id         BIGINT,
    quantity_change INT      NOT NULL,
    movement_date   DATETIME DEFAULT GETDATE(),
    is_deleted      BIT      DEFAULT 0 NOT NULL,

    CONSTRAINT fk_movements_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_movements_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL
);
```

| Cột | Kiểu dữ liệu | Ràng buộc | Mục đích |
|-----|-------------|-----------|---------|
| `movement_id` | BIGINT | PK, AUTO_INCREMENT | Định danh duy nhất |
| `product_id` | BIGINT | FK → products, NOT NULL | Sản phẩm liên quan |
| `user_id` | BIGINT | FK → users, Nullable | Người thực hiện |
| `quantity_change` | INT | NOT NULL | Dương = nhập kho · Âm = xuất kho |
| `movement_date` | DATETIME | DEFAULT GETDATE() | Thời điểm thực hiện giao dịch |
| `is_deleted` | BIT | DEFAULT 0, NOT NULL | Soft Delete flag |

**Ghi chú thiết kế:**
- `quantity_change` mang dấu: dương (+) là nhập kho, âm (-) là xuất kho.
- `user_id` Nullable: phiếu kho có thể được tạo bởi "System" (không có user đăng nhập).
- `ON DELETE CASCADE` trên `product_id`: về lý thuyết, nếu sản phẩm bị xóa cứng, phiếu kho bị xóa theo — nhưng với Soft Delete thì điều này không xảy ra.
- Phiếu kho **không được sửa** (không có API PUT) — chỉ tạo mới hoặc xóa mềm để đảm bảo Audit Trail.

---

## 3. SƠ ĐỒ QUAN HỆ (Entity Relationship)

```
┌──────────────┐          ┌──────────────────┐
│   suppliers  │          │      users       │
├──────────────┤          ├──────────────────┤
│ supplier_id  │◄──┐      │ user_id          │◄─────────────┐
│ supplier_name│   │      │ username         │              │
│ email        │   │      │ password         │              │
│ phone        │   │      │ full_name        │              │
│ is_deleted   │   │      │ email            │              │
└──────────────┘   │      │ role             │              │
                   │      │ is_deleted       │              │
                   │      │ created_at       │              │
                   │      └──────────────────┘              │
                   │                                        │
            1      │ N                               1      │ N
            ▼      │                                 ▼      │
┌──────────────────┤          ┌─────────────────────────────┘
│     products     │          │        stock_movements       │
├──────────────────┤          ├──────────────────────────────┤
│ product_id    ◄──┼──────────│ product_id (FK, NOT NULL)    │
│ supplier_id (FK) │    1  N  │ user_id (FK, nullable)       │
│ product_name     │          │ movement_id                  │
│ quantity_in_stock│          │ quantity_change               │
│ status           │          │ movement_date                │
│ is_deleted       │          │ is_deleted                   │
└──────────────────┘          └──────────────────────────────┘
```

---

## 4. LUỒNG DỮ LIỆU KHI THỰC HIỆN CÁC THAO TÁC

### 4.1. Nhập kho

```
[Client] POST /api/stock-movements
  { productId: 1, quantityChange: 50 }
        ↓
[Service] Đọc products WHERE product_id = 1
  → Kiểm tra status: Nếu status == "SUSPENDED" → Báo lỗi 400 (Cannot perform stock movement on suspended product)
  quantity_in_stock hiện tại = 10
        ↓
[Service] Tính: newStock = 10 + 50 = 60
  → Hợp lệ (>= 0)
        ↓
[DB] UPDATE products SET quantity_in_stock = 60 WHERE product_id = 1
[DB] INSERT INTO stock_movements (product_id, quantity_change, ...) VALUES (1, 50, ...)
        ↓
[Response] 201 Created · StockMovementDto
```

### 4.2. Xuất kho

```
[Client] POST /api/stock-movements
  { productId: 1, quantityChange: -100 }
        ↓
[Service] Đọc products WHERE product_id = 1
  → Kiểm tra status: Nếu status == "SUSPENDED" → Báo lỗi 400 (Chặn xuất kho sản phẩm tạm ngưng bán)
  quantity_in_stock hiện tại = 60
        ↓
[Service] Tính: newStock = 60 + (-100) = -40
  → KHÔNG hợp lệ (< 0)
        ↓
[Response] 400 Bad Request
  { success: false, message: "Insufficient stock in warehouse..." }
  -- Không có bản ghi nào được ghi vào DB --
```

### 4.3. Chuyển sản phẩm vào Thùng rác (Soft Delete)

```
[Client] DELETE /api/products/1
        ↓
[Service] Kiểm tra: nếu quantity_in_stock > 0 → cảnh báo/chặn hoặc xác nhận
        ↓
[Hibernate @SQLDelete]
  Thực thi: UPDATE products SET is_deleted = 1 WHERE product_id = 1
        ↓
[@SQLRestriction("is_deleted = 0")]
  Mọi SELECT thông thường tự lọc: ... WHERE is_deleted = 0
  → product_id = 1 chuyển sang danh sách Thùng rác (Recycle Bin - /api/products/trash)
  -- Không có nút xóa vĩnh viễn (Hard delete), bảo vệ toàn vẹn lịch sử phiếu kho --
```

### 4.4. Hủy phiếu kho

```
[Client] DELETE /api/stock-movements/5
  (Phiếu #5: quantity_change = +20)
        ↓
[Service] Đọc stock_movements WHERE movement_id = 5
  quantity_change = +20
  product hiện tại: quantity_in_stock = 35
        ↓
[Service] Tính: revertedStock = 35 - 20 = 15
  → Hợp lệ (>= 0)
        ↓
[DB] UPDATE products SET quantity_in_stock = 15 WHERE product_id = X
[DB] UPDATE stock_movements SET is_deleted = 1 WHERE movement_id = 5
        ↓
[Response] 200 OK · tồn kho đã được hoàn tác
```

### 4.5. Tạm dừng / Mở lại kinh doanh (Suspend / Reactivate)

```
[Client] PATCH /api/products/{id}/status
        ↓
[Service] Đổi trạng thái:
  - Nếu hiện tại là ACTIVE → chuyển thành SUSPENDED
  - Nếu hiện tại là SUSPENDED → chuyển thành ACTIVE
        ↓
[DB] UPDATE products SET status = 'SUSPENDED' WHERE product_id = {id}
        ↓
[Response] 200 OK · ProductDto (cập nhật badge trạng thái, chặn các giao dịch xuất kho)
```

### 4.6. Khôi phục sản phẩm từ Thùng rác (Restore with Stock Update)

```
[Client] PUT /api/products/{id}/restore
  Body: { quantityInStock: 25 } (tùy chọn điều chỉnh tồn kho)
        ↓
[Service] Tìm sản phẩm bằng Native Query / Custom Repo (bỏ qua bộ lọc is_deleted = 0)
        ↓
[DB] UPDATE products SET is_deleted = 0, status = 'ACTIVE', quantity_in_stock = 25 WHERE product_id = {id}
        ↓
[Response] 200 OK · ProductDto (sản phẩm xuất hiện lại trên Product List)
```

---

## 5. CÁC CHỈ MỤC VÀ RÀNG BUỘC

| Bảng | Loại | Cột | Mục đích |
|------|------|-----|---------|
| users | UNIQUE INDEX | username | Không cho phép tên đăng nhập trùng |
| users | UNIQUE INDEX | email | Không cho phép email trùng |
| products | FOREIGN KEY | supplier_id → suppliers | Toàn vẹn tham chiếu |
| stock_movements | FOREIGN KEY | product_id → products | Toàn vẹn tham chiếu (CASCADE) |
| stock_movements | FOREIGN KEY | user_id → users | Toàn vẹn tham chiếu (SET NULL) |

---

## 6. SCRIPT TẠO BẢNG (Idempotent)

File [`database/schema.sql`](./database/schema.sql) được thiết kế idempotent — chạy nhiều lần không bị lỗi:

```sql
-- Kiểm tra bảng đã tồn tại chưa trước khi CREATE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
    CREATE TABLE products (...);
END
ELSE
BEGIN
    -- Kiểm tra cột is_deleted đã có chưa trước khi ALTER
    IF NOT EXISTS (SELECT * FROM sys.columns
                   WHERE object_id = OBJECT_ID('products') AND name = 'is_deleted')
    BEGIN
        ALTER TABLE products ADD is_deleted BIT DEFAULT 0 NOT NULL;
    END

    -- Kiểm tra cột status đã có chưa trước khi ALTER
    IF NOT EXISTS (SELECT * FROM sys.columns
                   WHERE object_id = OBJECT_ID('products') AND name = 'status')
    BEGIN
        ALTER TABLE products ADD status NVARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;
    END
END
GO
```

Cách tiếp cận này đảm bảo:
- Chạy lần đầu: tạo bảng đầy đủ.
- Chạy trên DB cũ chưa có cột `is_deleted` hoặc `status`: tự động thêm cột tương thích ngược.
- Chạy trên DB đã đầy đủ: không làm gì, không lỗi.

---

## 7. DỮ LIỆU MẪU (Seed Data)

Hệ thống tự tạo dữ liệu mẫu qua `DataInitializer.java` khi khởi động lần đầu (kiểm tra `userRepository.count() == 0`):

### Tài khoản mặc định

| username | password | role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| staff | staff123 | STAFF |

### Nhà cung cấp mặc định

| supplier_id | supplier_name | email | phone |
|:-----------:|---------------|-------|-------|
| 1 | TechLink Distribution | contact@techlink.com | 0901234567 |
| 2 | Global Hardware Co. | sales@globalhw.com | 0912345678 |
| 3 | Apex Components | support@apexcomp.vn | 0987654321 |

### Sản phẩm mặc định

| product_id | product_name | supplier | quantity_in_stock |
|:----------:|--------------|---------|:-----------------:|
| 1 | Mechanical Gaming Keyboard | TechLink | 45 |
| 2 | Wireless Ergonomic Mouse | TechLink | 8 |
| 3 | UltraWide 34-inch Monitor | Global Hardware | 15 |
| 4 | USB-C Hub Multiport | Apex Components | 5 |

### Phiếu kho mẫu

| product_id | quantity_change | Ghi chú |
|:----------:|:---------------:|---------|
| 1 | +50 | Nhập kho 3 ngày trước |
| 1 | -5 | Xuất kho 1 ngày trước |
| 2 | +10 | Nhập kho 2 ngày trước |
| 2 | -2 | Xuất kho 5 giờ trước |
| 3 | +20 | Nhập kho 4 ngày trước |
| 3 | -5 | Xuất kho 2 ngày trước |
| 4 | +10 | Nhập kho 1 ngày trước |
| 4 | -5 | Xuất kho 2 giờ trước |

> **Tổng đại số khớp với tồn kho:** P1: 50-5=45 · P2: 10-2=8 · P3: 20-5=15 · P4: 10-5=5
