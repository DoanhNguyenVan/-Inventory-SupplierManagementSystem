# TÀI LIỆU NGHIỆP VỤ
## HỆ THỐNG QUẢN LÝ TỒN KHO & NHÀ CUNG CẤP (ISMS)
**Inventory & Supplier Management System**

---

## 1. TỔNG QUAN HỆ THỐNG

### 1.1. Mục tiêu

ISMS là hệ thống quản lý tồn kho và nhà cung cấp được thiết kế theo chuẩn ERP dành cho doanh nghiệp vừa và nhỏ. Hệ thống giải quyết 3 vấn đề cốt lõi:

| Vấn đề | Cách ISMS giải quyết |
|--------|----------------------|
| Số liệu tồn kho không nhất quán | Tồn kho chỉ thay đổi qua phiếu StockMovement, không cho phép sửa trực tiếp |
| Mất dữ liệu kiểm toán khi xóa | Áp dụng Soft Delete — bản ghi không bị xóa thật, chỉ đánh dấu is_deleted = 1 |
| Không kiểm soát quyền truy cập | Phân quyền 3 cấp: ADMIN / STAFF / USER |

### 1.2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|-----------|-----------|
| Backend Framework | Spring Boot 3.4.3 |
| Ngôn ngữ | Java 17 LTS |
| Persistence | Spring Data JPA + Hibernate 6.x |
| Database | Microsoft SQL Server 2016+ |
| Frontend | Thymeleaf + Bootstrap 5.3 + Vanilla JavaScript (Fetch API) |
| Bảo mật mật khẩu | PBKDF2WithHmacSHA256 (Salt 16 bytes, 65.536 vòng lặp) |

---

## 2. KIẾN TRÚC HỆ THỐNG

### 2.1. Cấu trúc phân tầng

```
PRESENTATION  →  Thymeleaf Templates + Static JS/CSS
CONTROLLER    →  REST API Controllers + Web Page Controllers
SERVICE       →  Business Logic + Validation Rules
REPOSITORY    →  Spring Data JPA (Hibernate 6)
DATABASE      →  Microsoft SQL Server (ISMS_DB)
```

### 2.2. Sơ đồ Entity Relationship

```
users                   suppliers
─────────               ─────────────
user_id (PK)            supplier_id (PK)
username                supplier_name
password                email
full_name               phone
email                   is_deleted
role
is_deleted                   │ 1
created_at                   │
     │ 1                     ▼ N
     │              products
     │              ────────────────
     │              product_id (PK)
     │              supplier_id (FK)
     │              product_name
     │              quantity_in_stock
     │              is_deleted
     │                   │ 1
     │                   │
     │ 1                 ▼ N
     └──────→ stock_movements
              ──────────────────
              movement_id (PK)
              product_id (FK)
              user_id (FK, nullable)
              quantity_change
              movement_date
              is_deleted
```

### 2.3. Quan hệ giữa các bảng

| Quan hệ | Loại | Ràng buộc |
|---------|------|-----------|
| suppliers → products | One-to-Many | ON DELETE SET NULL |
| products → stock_movements | One-to-Many | ON DELETE CASCADE |
| users → stock_movements | One-to-Many | Nullable (user_id có thể NULL) |

---

## 3. QUY TẮC NGHIỆP VỤ CỐT LÕI

### 3.1. Toàn vẹn tồn kho (Stock Integrity)

- Cột `quantity_in_stock` không có API sửa trực tiếp.
- Mọi thay đổi tồn kho phải thông qua tạo bản ghi `StockMovement`.
- Tồn kho hiện tại = Tổng đại số tất cả `quantity_change` trong lịch sử.
- Khi tạo sản phẩm mới, tồn kho mặc định luôn là 0.

Luồng cập nhật:
```
Nhập kho (quantity_change > 0):
  quantity_mới = quantity_hiện_tại + quantity_change

Xuất kho (quantity_change < 0):
  Nếu quantity_hiện_tại + quantity_change >= 0  → Cho phép
  Nếu quantity_hiện_tại + quantity_change <  0  → Từ chối (Insufficient stock)
```

### 3.2. Soft Delete (Xóa mềm)

Tất cả 4 bảng đều áp dụng Soft Delete qua annotation Hibernate:

- `@SQLDelete` → Hibernate chuyển DELETE thành `UPDATE ... SET is_deleted = 1`
- `@SQLRestriction("is_deleted = 0")` → Mọi SELECT tự động lọc bản ghi đã xóa

### 3.3. Hoàn tác tồn kho khi xóa phiếu

Khi xóa StockMovement:
1. Tính: `quantity_sau_hoàn_tác = quantity_hiện_tại - quantity_change_của_phiếu`
2. Nếu kết quả < 0 → Từ chối xóa
3. Nếu kết quả >= 0 → Xóa mềm phiếu + cập nhật lại `quantity_in_stock`

### 3.4. Phân quyền (RBAC)

| Quyền | ADMIN | STAFF | USER |
|-------|:-----:|:-----:|:----:|
| Xem sản phẩm / nhà cung cấp / phiếu kho | ✅ | ✅ | ✅ |
| Tạo / Sửa / Xóa sản phẩm | ✅ | ✅ | ❌ |
| Tạo / Sửa / Xóa nhà cung cấp | ✅ | ✅ | ❌ |
| Tạo / Xóa phiếu kho | ✅ | ✅ | ❌ |
| Xem / Quản lý người dùng | ✅ | ❌ | ❌ |

### 3.5. Bảo vệ Admin cuối cùng

- Không cho phép hạ quyền Admin duy nhất còn lại.
- Không cho phép xóa Admin duy nhất còn lại.
- Admin không thể tự xóa tài khoản đang đăng nhập của chính mình.

---

## 4. ĐẶC TẢ API

### 4.1. Authentication — `/api/auth`

| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| POST | `/api/auth/login` | Tất cả | Đăng nhập, tạo session |
| POST | `/api/auth/register` | Tất cả | Đăng ký, role mặc định USER |
| POST | `/api/auth/logout` | Đã đăng nhập | Hủy session |
| GET | `/api/auth/me` | Đã đăng nhập | Lấy thông tin phiên hiện tại |

### 4.2. Suppliers — `/api/suppliers`

| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| GET | `/api/suppliers` | Đã đăng nhập | Danh sách (hỗ trợ `?search=`) |
| GET | `/api/suppliers/{id}` | Đã đăng nhập | Chi tiết |
| GET | `/api/suppliers/{id}/products` | Đã đăng nhập | Sản phẩm của nhà cung cấp |
| POST | `/api/suppliers` | ADMIN/STAFF | Thêm mới |
| PUT | `/api/suppliers/{id}` | ADMIN/STAFF | Cập nhật |
| DELETE | `/api/suppliers/{id}` | ADMIN/STAFF | Xóa mềm (chặn nếu còn sản phẩm) |

### 4.3. Products — `/api/products`

| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| GET | `/api/products` | Đã đăng nhập | Danh sách (hỗ trợ `?search=`) |
| GET | `/api/products/{id}` | Đã đăng nhập | Chi tiết |
| POST | `/api/products` | ADMIN/STAFF | Tạo mới (stock = 0) |
| PUT | `/api/products/{id}` | ADMIN/STAFF | Cập nhật tên/nhà cung cấp |
| DELETE | `/api/products/{id}` | ADMIN/STAFF | Xóa mềm (chặn nếu còn tồn kho) |

### 4.4. Stock Movements — `/api/stock-movements`

| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| GET | `/api/stock-movements` | Đã đăng nhập | Toàn bộ lịch sử, sắp xếp mới nhất trước |
| GET | `/api/stock-movements/{id}` | Đã đăng nhập | Chi tiết phiếu |
| POST | `/api/stock-movements` | ADMIN/STAFF | Tạo phiếu nhập/xuất |
| DELETE | `/api/stock-movements/{id}` | ADMIN/STAFF | Hủy phiếu + hoàn tác tồn kho |

### 4.5. Users — `/api/users`

| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| GET | `/api/users` | ADMIN | Danh sách (hỗ trợ `?search=`) |
| GET | `/api/users/{id}` | ADMIN | Chi tiết |
| PUT | `/api/users/{id}/role` | ADMIN | Thay đổi quyền |
| DELETE | `/api/users/{id}` | ADMIN | Xóa mềm |

### 4.6. Dashboard — `/api/dashboard`

| Method | Endpoint | Quyền | Mô tả |
|--------|----------|-------|-------|
| GET | `/api/dashboard/stats` | Đã đăng nhập | Tổng sản phẩm, nhà cung cấp, hàng sắp hết (<10), tổng phiếu kho |

---

## 5. XỬ LÝ LỖI

Mọi lỗi trả về định dạng JSON nhất quán:

```json
{
  "success": false,
  "message": "Mô tả lỗi cụ thể",
  "data": null
}
```

| Loại ngoại lệ | HTTP Status |
|--------------|-------------|
| IllegalArgumentException | 400 Bad Request |
| MethodArgumentNotValidException | 400 Bad Request |
| HttpMessageNotReadableException | 400 Bad Request |
| DataIntegrityViolationException | 400 Bad Request |
| Lỗi hệ thống khác | 500 Internal Server Error |

---

## 6. VALIDATION DỮ LIỆU ĐẦU VÀO

### User
| Trường | Quy tắc |
|--------|---------|
| username | Bắt buộc, ≤50 ký tự, chỉ chứa `a-z A-Z 0-9 . _ -` |
| password | ≥6 ký tự |
| fullName | ≤100 ký tự |
| email | Đúng định dạng, ≤100 ký tự, không trùng |

### Supplier
| Trường | Quy tắc |
|--------|---------|
| supplierName | Bắt buộc, ≤100 ký tự |
| email | Đúng định dạng, ≤100 ký tự |
| phone | 4–20 ký tự, chứa ký tự hợp lệ `+ 0-9 - ( ) space` |

### Product
| Trường | Quy tắc |
|--------|---------|
| productName | Bắt buộc, ≤150 ký tự |
| supplierId | Bắt buộc, nhà cung cấp phải tồn tại |

### StockMovement
| Trường | Quy tắc |
|--------|---------|
| productId | Bắt buộc, sản phẩm phải tồn tại |
| quantityChange | Khác 0, trong khoảng ±1.000.000 |
| movementDate | Định dạng: `yyyy-MM-dd'T'HH:mm:ss` hoặc `yyyy-MM-dd HH:mm` |

---

## 7. BẢO MẬT

### Hash mật khẩu

```
Thuật toán : PBKDF2WithHmacSHA256
Salt       : 16 bytes ngẫu nhiên (SecureRandom)
Iterations : 65.536
Key Length : 256 bits
Lưu dạng   : Base64(salt) + ":" + Base64(hash)
```

### Session

- Xác thực thủ công qua session cookie `JSESSIONID`.
- Kiểm tra session ở tầng Controller trước mọi thao tác nghiệp vụ.
- `UserDto` không chứa trường `password` — mật khẩu không bao giờ trả về client.
