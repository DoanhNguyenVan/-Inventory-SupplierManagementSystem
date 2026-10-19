# BẢNG ĐẶC TẢ TEST CASES
## HỆ THỐNG QUẢN LÝ TỒN KHO & NHÀ CUNG CẤP (ISMS)

---

## 1. QUY ƯỚC

| Ký hiệu | Ý nghĩa |
|---------|---------|
| **Positive** | Kịch bản đúng — hệ thống xử lý thành công |
| **Negative** | Kịch bản sai dữ liệu — hệ thống phải báo lỗi |
| **Security** | Kiểm tra quyền truy cập và bảo mật |
| **Data Integrity** | Kiểm tra toàn vẹn dữ liệu và Soft Delete |

---

## 2. PHÂN HỆ 1: XÁC THỰC (AUTHENTICATION & RBAC)

| Mã Test | Tên | Loại | Endpoint | Dữ liệu đầu vào | Kết quả mong đợi |
|---------|-----|------|----------|-----------------|------------------|
| TC_AUTH_01 | Đăng nhập Admin | Positive | POST /api/auth/login | `username: "admin"`, `password: "admin123"` | 200 OK · role: `"ADMIN"` · cookie JSESSIONID được cấp |
| TC_AUTH_02 | Đăng nhập Staff | Positive | POST /api/auth/login | `username: "staff"`, `password: "staff123"` | 200 OK · role: `"STAFF"` |
| TC_AUTH_03 | Đăng ký tài khoản mới | Positive | POST /api/auth/register | `username: "testuser"`, `password: "pass123"`, `email: "test@isms.com"` | 201 Created · role mặc định: `"USER"` |
| TC_AUTH_04 | Đăng nhập sai mật khẩu | Negative | POST /api/auth/login | `username: "admin"`, `password: "wrongpass"` | 401 Unauthorized · `"Invalid username or password"` |
| TC_AUTH_05 | Đăng nhập username không tồn tại | Negative | POST /api/auth/login | `username: "ghost_user"`, `password: "123456"` | 401 Unauthorized · `"Invalid username or password"` |
| TC_AUTH_06 | Đăng ký trùng username | Negative | POST /api/auth/register | `username: "admin"` | 400 Bad Request · `"Username 'admin' is already taken"` |
| TC_AUTH_07 | Đăng ký mật khẩu quá ngắn (<6 ký tự) | Negative | POST /api/auth/register | `password: "123"` | 400 Bad Request · `"Password must be at least 6 characters long"` |
| TC_AUTH_08 | Lấy thông tin phiên đang đăng nhập | Positive | GET /api/auth/me | Cookie session hợp lệ | 200 OK · thông tin user hiện tại |
| TC_AUTH_09 | Lấy thông tin khi chưa đăng nhập | Security | GET /api/auth/me | Không có cookie | 401 Unauthorized · `"Not authenticated"` |
| TC_AUTH_10 | Đăng xuất | Positive | POST /api/auth/logout | Session hợp lệ | 200 OK · session bị hủy |

---

## 3. PHÂN HỆ 2: QUẢN LÝ NHÀ CUNG CẤP (SUPPLIER MANAGEMENT)

| Mã Test | Tên | Loại | Endpoint | Dữ liệu đầu vào | Kết quả mong đợi |
|---------|-----|------|----------|-----------------|------------------|
| TC_SUPP_01 | Lấy danh sách nhà cung cấp | Positive | GET /api/suppliers | Session hợp lệ | 200 OK · danh sách supplier có `is_deleted = 0` |
| TC_SUPP_02 | Tìm kiếm theo tên | Positive | GET /api/suppliers?search=Tech | — | 200 OK · trả về `TechLink Distribution` |
| TC_SUPP_03 | Thêm nhà cung cấp hợp lệ | Positive | POST /api/suppliers | `supplierName: "An Phat"`, `phone: "0988112233"`, `email: "ap@anphat.vn"` | 201 Created · supplierId mới |
| TC_SUPP_04 | Thêm nhà cung cấp để trống tên | Negative | POST /api/suppliers | `supplierName: " "` | 400 Bad Request · `"Supplier name is required"` |
| TC_SUPP_05 | SĐT vượt quá 20 ký tự | Negative | POST /api/suppliers | `phone: "0123456789012345678901234"` | 400 Bad Request · `"Supplier phone number cannot exceed 20 characters"` |
| TC_SUPP_06 | Email sai định dạng | Negative | POST /api/suppliers | `email: "sai_dinh_dang"` | 400 Bad Request · `"Invalid supplier email format"` |
| TC_SUPP_07 | Cập nhật thông tin nhà cung cấp | Positive | PUT /api/suppliers/{id} | Tên mới, SĐT mới | 200 OK · dữ liệu được cập nhật |
| TC_SUPP_08 | Xóa nhà cung cấp đang có sản phẩm | Data Integrity | DELETE /api/suppliers/{id} | id có sản phẩm liên kết | 400 Bad Request · `"Cannot delete supplier... linked product(s)"` |
| TC_SUPP_09 | Xóa mềm nhà cung cấp không có sản phẩm | Data Integrity | DELETE /api/suppliers/{id} | id không có sản phẩm | 200 OK · DB: `is_deleted = 1` · không hiện trên giao diện |
| TC_SUPP_10 | Truy cập API khi chưa đăng nhập | Security | GET /api/suppliers | Không có cookie | 401 Unauthorized · `"Authentication required. Please sign in."` |

---

## 4. PHÂN HỆ 3: QUẢN LÝ SẢN PHẨM (PRODUCT MANAGEMENT)

| Mã Test | Tên | Loại | Endpoint | Dữ liệu đầu vào | Kết quả mong đợi |
|---------|-----|------|----------|-----------------|------------------|
| TC_PROD_01 | Lấy danh sách sản phẩm | Positive | GET /api/products | Session hợp lệ | 200 OK · danh sách có `is_deleted = 0` |
| TC_PROD_02 | Tìm kiếm sản phẩm theo tên | Positive | GET /api/products?search=Keyboard | — | 200 OK · trả về sản phẩm có "Keyboard" trong tên |
| TC_PROD_03 | Tạo sản phẩm mới | Positive | POST /api/products | `productName: "Dell U2723QE"`, `supplierId: 1` | 201 Created · `quantityInStock: 0` |
| TC_PROD_04 | Tạo sản phẩm không chọn nhà cung cấp | Negative | POST /api/products | `supplierId: null` | 400 Bad Request · `"Supplier is required for this product!"` |
| TC_PROD_05 | Tên sản phẩm vượt 150 ký tự | Negative | POST /api/products | `productName: [160 ký tự]` | 400 Bad Request · `"Product name cannot exceed 150 characters!"` |
| TC_PROD_06 | Cập nhật thông tin sản phẩm | Positive | PUT /api/products/{id} | Tên mới | 200 OK · tên cập nhật, tồn kho giữ nguyên |
| TC_PROD_07 | Xóa sản phẩm chuyển vào Thùng rác (có tồn kho) | Data Integrity | DELETE /api/products/{id} | id có `quantity_in_stock > 0` | 200 OK · Cảnh báo tồn kho · chuyển sang tab Thùng rác (`is_deleted = 1`) |
| TC_PROD_08 | Xóa mềm sản phẩm khi tồn kho = 0 | Data Integrity | DELETE /api/products/{id} | id có `quantity_in_stock = 0` | 200 OK · DB: `is_deleted = 1` · chuyển sang Thùng rác · lịch sử phiếu kho giữ nguyên |
| TC_PROD_09 | Role USER tạo sản phẩm | Security | POST /api/products | Session role USER | 403 Forbidden · `"Access denied: Role 'USER' is restricted to read-only access."` |
| TC_PROD_10 | Tạm ngưng kinh doanh sản phẩm (Suspend) | Positive | PATCH /api/products/{id}/status | id sản phẩm đang ACTIVE | 200 OK · `status = "SUSPENDED"` · badge hiển thị Suspended |
| TC_PROD_11 | Kích hoạt lại sản phẩm (Reactivate) | Positive | PATCH /api/products/{id}/status | id sản phẩm đang SUSPENDED | 200 OK · `status = "ACTIVE"` · cho phép xuất nhập kho trở lại |
| TC_PROD_12 | Xem danh sách Thùng rác (Recycle Bin) | Positive | GET /api/products/trash | Session hợp lệ | 200 OK · danh sách sản phẩm có `is_deleted = 1` |
| TC_PROD_13 | Khôi phục sản phẩm từ Thùng rác (Restore) | Positive | PUT /api/products/{id}/restore | Body: `{"quantityInStock": 20}` | 200 OK · `is_deleted = 0`, `status = "ACTIVE"`, tồn kho cập nhật |

---

## 5. PHÂN HỆ 4: BIẾN ĐỘNG KHO (STOCK MOVEMENTS)

| Mã Test | Tên | Loại | Endpoint | Dữ liệu đầu vào | Kết quả mong đợi |
|---------|-----|------|----------|-----------------|------------------|
| TC_STOCK_01 | Lấy lịch sử phiếu kho | Positive | GET /api/stock-movements | Session hợp lệ | 200 OK · danh sách giảm dần theo ngày |
| TC_STOCK_02 | Tạo phiếu Nhập kho (+) | Positive | POST /api/stock-movements | `productId: 1`, `quantityChange: 30` | 201 Created · tồn kho tăng 30 · ghi tên người tạo |
| TC_STOCK_03 | Tạo phiếu Xuất kho (-) | Positive | POST /api/stock-movements | `productId: 1`, `quantityChange: -10` | 201 Created · tồn kho giảm 10 |
| TC_STOCK_04 | Xuất vượt số lượng tồn kho | Data Integrity | POST /api/stock-movements | `quantityChange: -99999` (lớn hơn tồn kho) | 400 Bad Request · `"Insufficient stock in warehouse!"` · tồn kho không thay đổi |
| TC_STOCK_05 | Số lượng = 0 | Negative | POST /api/stock-movements | `quantityChange: 0` | 400 Bad Request · `"Quantity change cannot be zero!"` |
| TC_STOCK_06 | Số lượng vượt 1.000.000 | Negative | POST /api/stock-movements | `quantityChange: 5000000` | 400 Bad Request · `"Quantity change cannot exceed 1,000,000 units"` |
| TC_STOCK_07 | Xóa phiếu và hoàn tác tồn kho | Data Integrity | DELETE /api/stock-movements/{id} | id phiếu nhập 20 | 200 OK · tồn kho giảm 20 · phiếu bị xóa mềm |
| TC_STOCK_08 | Xóa phiếu gây âm tồn kho | Data Integrity | DELETE /api/stock-movements/{id} | Xóa phiếu nhập khi đã xuất gần hết | 400 Bad Request · `"Cannot delete this stock movement because it would cause warehouse stock... to become negative"` |
| TC_STOCK_09 | Role USER tạo phiếu kho | Security | POST /api/stock-movements | Session role USER | 403 Forbidden |
| TC_STOCK_10 | Chặn biến động kho với sản phẩm SUSPENDED | Negative | POST /api/stock-movements | `productId` của sản phẩm đang SUSPENDED | 400 Bad Request · `"Cannot perform stock movement on a suspended product! Please reactivate the product first."` |

---

## 6. PHÂN HỆ 5: QUẢN TRỊ NGƯỜI DÙNG (USER MANAGEMENT)

| Mã Test | Tên | Loại | Endpoint | Dữ liệu đầu vào | Kết quả mong đợi |
|---------|-----|------|----------|-----------------|------------------|
| TC_USER_01 | Admin xem danh sách người dùng | Positive | GET /api/users | Session ADMIN | 200 OK · danh sách user đang hoạt động |
| TC_USER_02 | Staff/User xem danh sách user | Security | GET /api/users | Session STAFF/USER | 403 Forbidden · `"Access denied: Administrator privilege required"` |
| TC_USER_03 | Nâng quyền tài khoản lên STAFF | Positive | PUT /api/users/{id}/role | `role: "STAFF"` | 200 OK · role được cập nhật |
| TC_USER_04 | Gán role không hợp lệ | Negative | PUT /api/users/{id}/role | `role: "MANAGER_VIP"` | 400 Bad Request · `"Invalid role. Permitted roles: ADMIN, STAFF, USER"` |
| TC_USER_05 | Hạ quyền Admin duy nhất | Data Integrity | PUT /api/users/{id}/role | `role: "USER"` (Admin cuối) | 400 Bad Request · `"Cannot demote the last remaining Administrator in the system"` |
| TC_USER_06 | Admin xóa tài khoản nhân viên | Positive | DELETE /api/users/{id} | id của Staff | 200 OK · DB: `is_deleted = 1` · tài khoản không thể đăng nhập |
| TC_USER_07 | Admin tự xóa chính mình | Security | DELETE /api/users/{id} | id của chính Admin đang đăng nhập | 400 Bad Request · `"You cannot delete your own logged-in account"` |

---

## 7. PHÂN HỆ 6: BẢO MẬT & XỬ LÝ NGOẠI LỆ

| Mã Test | Tên | Loại | Endpoint | Dữ liệu đầu vào | Kết quả mong đợi |
|---------|-----|------|----------|-----------------|------------------|
| TC_SEC_01 | Gọi API sản phẩm không có session | Security | GET /api/products | Không có cookie | 401 Unauthorized · JSON `{success: false, message: "Authentication required..."}` |
| TC_SEC_02 | Gọi API dashboard không có session | Security | GET /api/dashboard/stats | Không có cookie | 401 Unauthorized |
| TC_SEC_03 | Gửi JSON không hợp lệ | Negative | POST /api/products | Body: `{invalid json}` | 400 Bad Request · `"Malformed JSON request or invalid data/date format."` |
| TC_SEC_04 | Ngày giờ sai định dạng trong phiếu kho | Negative | POST /api/stock-movements | `movementDate: "ngày không đúng"` | 400 Bad Request · thông báo lỗi parse ngày |
| TC_SEC_05 | Response không bao giờ chứa password | Security | GET /api/auth/me | Session hợp lệ | 200 OK · JSON không có trường `password` |

---

## 8. TÓM TẮT ĐỘ PHỦ TEST

| Phân hệ | Số test case | Positive | Negative | Security | Data Integrity |
|---------|:------------:|:--------:|:--------:|:--------:|:--------------:|
| Authentication | 10 | 4 | 3 | 2 | 1 |
| Supplier | 10 | 4 | 3 | 1 | 2 |
| Product | 13 | 8 | 2 | 1 | 2 |
| Stock Movement | 10 | 3 | 3 | 1 | 3 |
| User Management | 7 | 3 | 1 | 2 | 1 |
| Security/Exception | 5 | 0 | 2 | 3 | 0 |
| **TỔNG** | **55** | **22** | **14** | **10** | **9** |
