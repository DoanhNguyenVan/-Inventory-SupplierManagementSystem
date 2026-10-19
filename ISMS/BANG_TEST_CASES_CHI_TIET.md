# BẢNG ĐẶC TẢ TEST CASES CHI TIẾT
## DỰ ÁN HỆ THỐNG QUẢN LÝ TỒN KHO & NHÀ CUNG CẤP (ISMS)

---

## 1. Bảng Ký Hiệu & Quy Ước
- **Phân loại**:
  - `Positive`: Kịch bản thành công (Happy Path).
  - `Negative`: Kịch bản kiểm thử lỗi & validation ngoại lệ.
  - `Security`: Kiểm thử bảo mật & phân quyền truy cập.
  - `Data Integrity`: Kiểm thử tính toàn vẹn dữ liệu và xóa mềm.
- **Trạng thái**: `PASS` (Đã kiểm thử và chạy thành công).

---

## 2. Danh Sách Test Cases Chi Tiết

### PHÂN HỆ 1: XÁC THỰC & PHÂN QUYỀN (AUTHENTICATION & RBAC)

| Mã Test Case | Tên Chức Năng | Phân Loại | Các Bước Thực Hiện | Dữ Liệu Đầu Vào | Kết Quả Mong Đợi | Trạng Thái |
| :--- | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_AUTH_01** | Đăng nhập tài khoản Admin | Positive | 1. Gửi request `POST /api/auth/login`<br>2. Kiểm tra mã phản hồi và role | `username: "admin"`<br>`password: "admin123"` | HTTP 200 OK. `role: "ADMIN"`. Cookie `JSESSIONID` được cấp. | **PASS** |
| **TC_AUTH_02** | Đăng nhập tài khoản Staff | Positive | 1. Gửi request `POST /api/auth/login`<br>2. Kiểm tra role trả về | `username: "staff"`<br>`password: "staff123"` | HTTP 200 OK. `role: "STAFF"`. | **PASS** |
| **TC_AUTH_03** | Đăng ký tài khoản mới | Positive | 1. Gửi `POST /api/auth/register`<br>2. Kiểm tra tạo user trong DB | `username: "testuser"`<br>`password: "pass123"`<br>`email: "test@isms.com"` | HTTP 201 Created. Role mặc định là `"USER"`. | **PASS** |
| **TC_AUTH_04** | Đăng nhập sai mật khẩu | Negative | 1. Nhập đúng username, sai password | `username: "admin"`<br>`password: "wrongpass"` | HTTP 401 Unauthorized. Báo `"Invalid username or password"`. | **PASS** |
| **TC_AUTH_05** | Đăng nhập tài khoản không tồn tại | Negative | 1. Nhập username chưa từng đăng ký | `username: "ghost_user"`<br>`password: "123456"` | HTTP 401 Unauthorized. Báo `"Invalid username or password"`. | **PASS** |
| **TC_AUTH_06** | Đăng ký trùng Username | Negative | 1. Đăng ký với username đã có trong DB | `username: "admin"` | HTTP 400 Bad Request. Báo `"Username 'admin' is already taken"`. | **PASS** |
| **TC_AUTH_07** | Đăng ký mật khẩu quá ngắn (< 6 ký tự) | Negative | 1. Nhập mật khẩu 3 ký tự | `password: "123"` | HTTP 400 Bad Request. Báo `"Password must be at least 6 characters long"`. | **PASS** |
| **TC_AUTH_08** | Lấy thông tin phiên làm việc (`Me`) | Positive | 1. Đã đăng nhập<br>2. Gửi `GET /api/auth/me` | Cookie session hợp lệ | HTTP 200 OK. Trả về đúng thông tin user đang đăng nhập. | **PASS** |
| **TC_AUTH_09** | Lấy thông tin khi chưa đăng nhập | Security | 1. Xóa cookie session<br>2. Gửi `GET /api/auth/me` | Không có Cookie | HTTP 401 Unauthorized. Báo `"Not authenticated"`. | **PASS** |
| **TC_AUTH_10** | Đăng xuất tài khoản | Positive | 1. Gửi `POST /api/auth/logout` | Session đang hoạt động | HTTP 200 OK. Session bị hủy (invalidate). | **PASS** |

---

### PHÂN HỆ 2: QUẢN LÝ NHÀ CUNG CẤP (SUPPLIER MANAGEMENT)

| Mã Test Case | Tên Chức Năng | Phân Loại | Các Bước Thực Hiện | Dữ Liệu Đầu Vào | Kết Quả Mong Đợi | Trạng Thái |
| :--- | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_SUPP_01** | Lấy danh sách nhà cung cấp | Positive | 1. Đăng nhập<br>2. Gửi `GET /api/suppliers` | Header Session | HTTP 200 OK. Trả về danh sách đối tác không bị xóa mềm (`is_deleted = 0`). | **PASS** |
| **TC_SUPP_02** | Tìm kiếm nhà cung cấp theo tên | Positive | 1. Gửi `GET /api/suppliers?search=Tech` | `search: "Tech"` | HTTP 200 OK. Trả về đúng `TechLink Distribution`. | **PASS** |
| **TC_SUPP_03** | Thêm mới nhà cung cấp hợp lệ | Positive | 1. Gửi `POST /api/suppliers` | `supplierName: "An Phat"`<br>`phone: "0988112233"`<br>`email: "ap@anphat.vn"` | HTTP 201 Created. Trả về `supplierId` mới. | **PASS** |
| **TC_SUPP_04** | Thêm nhà cung cấp để trống tên | Negative | 1. Gửi `POST /api/suppliers` với tên rỗng | `supplierName: " "` | HTTP 400 Bad Request. Báo `"Supplier name is required"`. | **PASS** |
| **TC_SUPP_05** | Thêm SĐT vượt quá độ dài database | Negative | 1. Gửi SĐT dài 25 ký tự (> 20 ký tự DB) | `phone: "0123456789012345678901234"` | HTTP 400 Bad Request (Đã chặn ở Service, không bị crash 500). | **PASS** |
| **TC_SUPP_06** | Thêm email sai định dạng | Negative | 1. Gửi chuỗi không phải email | `email: "sai_dinh_dang"` | HTTP 400 Bad Request. Báo `"Invalid supplier email format"`. | **PASS** |
| **TC_SUPP_07** | Cập nhật thông tin nhà cung cấp | Positive | 1. Gửi `PUT /api/suppliers/{id}` | Tên mới, SĐT mới | HTTP 200 OK. Dữ liệu đối tác được cập nhật. | **PASS** |
| **TC_SUPP_08** | Chặn xóa nhà cung cấp đang có sản phẩm | Data Integrity | 1. Thử xóa đối tác ID: 1 (đang có 2 sản phẩm) | `DELETE /api/suppliers/1` | HTTP 400 Bad Request. Báo `"Cannot delete supplier... linked product(s)"`. | **PASS** |
| **TC_SUPP_09** | Xóa mềm nhà cung cấp không có sản phẩm | Data Integrity | 1. Tạo đối tác mới (chưa gắn sản phẩm)<br>2. Bấm xóa đối tác | `DELETE /api/suppliers/{new_id}` | HTTP 200 OK. Trong DB: `is_deleted = 1`. Trên web: biến mất. | **PASS** |
| **TC_SUPP_10** | Rò rỉ dữ liệu khi chưa đăng nhập | Security | 1. Không có session<br>2. Gửi `GET /api/suppliers` | Không Cookie | HTTP 401 Unauthorized. Báo `"Authentication required. Please sign in."`. | **PASS** |

---

### PHÂN HỆ 3: QUẢN LÝ SẢN PHẨM (PRODUCT MANAGEMENT)

| Mã Test Case | Tên Chức Năng | Phân Loại | Các Bước Thực Hiện | Dữ Liệu Đầu Vào | Kết Quả Mong Đợi | Trạng Thái |
| :--- | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_PROD_01** | Lấy danh sách sản phẩm | Positive | 1. Đăng nhập<br>2. Gửi `GET /api/products` | Header Session | HTTP 200 OK. Trả về danh mục sản phẩm đang kinh doanh (`is_deleted = 0`). | **PASS** |
| **TC_PROD_02** | Tìm kiếm sản phẩm theo tên | Positive | 1. Gửi `GET /api/products?search=Keyboard` | `search: "Keyboard"` | HTTP 200 OK. Trả về các loại bàn phím. | **PASS** |
| **TC_PROD_03** | Tạo sản phẩm mới (Tồn kho mặc định = 0) | Positive | 1. Gửi `POST /api/products` với tên và supplierId | `productName: "Dell U2723QE"`<br>`supplierId: 1` | HTTP 201 Created. `quantityInStock: 0`. | **PASS** |
| **TC_PROD_04** | Tạo sản phẩm không chọn nhà cung cấp | Negative | 1. Gửi `POST /api/products` với `supplierId: null` | `supplierId: null` | HTTP 400 Bad Request. Báo `"Supplier is required for this product!"`. | **PASS** |
| **TC_PROD_05** | Tạo sản phẩm với tên vượt quá 150 ký tự | Negative | 1. Gửi tên sản phẩm 160 ký tự | `productName: [160 chars]` | HTTP 400 Bad Request. Báo `"Product name cannot exceed 150 characters!"`. | **PASS** |
| **TC_PROD_06** | Cập nhật thông tin sản phẩm | Positive | 1. Gửi `PUT /api/products/{id}` | Tên sản phẩm mới | HTTP 200 OK. Tên mới được cập nhật, giữ nguyên số lượng tồn kho. | **PASS** |
| **TC_PROD_07** | Xóa sản phẩm chuyển vào Thùng rác (khi còn tồn kho) | Data Integrity | 1. Bấm xóa sản phẩm còn tồn kho > 0<br>2. Xác nhận hộp thoại cảnh báo | `DELETE /api/products/1` | HTTP 200 OK. Cảnh báo tồn kho > 0. Bản ghi chuyển sang tab Thùng rác (`is_deleted = 1`), không bị xóa vĩnh viễn khỏi DB. | **PASS** |
| **TC_PROD_08** | Xóa mềm sản phẩm khi số tồn = 0 | Data Integrity | 1. Sản phẩm có tồn kho = 0<br>2. Gửi `DELETE /api/products/{id}` | `DELETE /api/products/X` | HTTP 200 OK. Cột `is_deleted = 1`. Lịch sử xuất nhập kho cũ vẫn còn nguyên trong DB. | **PASS** |
| **TC_PROD_09** | Role USER thử tạo sản phẩm | Security | 1. Đăng nhập tài khoản role `USER`<br>2. Gửi `POST /api/products` | Role USER session | HTTP 403 Forbidden. Báo `"Access denied: Role 'USER' is restricted to read-only access."`. | **PASS** |
| **TC_PROD_10** | Tạm ngưng kinh doanh sản phẩm (Suspend) | Positive | 1. Bấm nút Suspend trên sản phẩm đang Active<br>2. Gửi `PATCH /api/products/{id}/status` | ID sản phẩm | HTTP 200 OK. `status: "SUSPENDED"`. Badge đổi sang màu xám. Nút đổi thành Reactivate. | **PASS** |
| **TC_PROD_11** | Kích hoạt lại sản phẩm tạm ngưng (Reactivate) | Positive | 1. Bấm nút Reactivate trên sản phẩm Suspended<br>2. Gửi `PATCH /api/products/{id}/status` | ID sản phẩm | HTTP 200 OK. `status: "ACTIVE"`. Badge đổi sang In Stock / Out of Stock. Cho phép xuất nhập kho trở lại. | **PASS** |
| **TC_PROD_12** | Xem danh mục Thùng rác (Recycle Bin) | Positive | 1. Bấm tab Thùng rác (Recycle Bin)<br>2. Gửi `GET /api/products/trash` | Header Session | HTTP 200 OK. Trả về đúng danh sách các sản phẩm có `is_deleted = 1`. | **PASS** |
| **TC_PROD_13** | Khôi phục sản phẩm từ Thùng rác (Restore) | Positive | 1. Trong tab Thùng rác, bấm Restore<br>2. Nhập số lượng tồn kho mới (nếu có)<br>3. Gửi `PUT /api/products/{id}/restore` | Body: `{"quantityInStock": 20}` | HTTP 200 OK. Sản phẩm được đưa trở lại Product List, `is_deleted = 0`, tồn kho cập nhật đúng 20 đơn vị. | **PASS** |

---

### PHÂN HỆ 4: BIẾN ĐỘNG KHO & KIỂM TOÁN TỒN KHO (STOCK MOVEMENTS)

| Mã Test Case | Tên Chức Năng | Phân Loại | Các Bước Thực Hiện | Dữ Liệu Đầu Vào | Kết Quả Mong Đợi | Trạng Thái |
| :--- | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_STOCK_01** | Lấy danh sách lịch sử kho | Positive | 1. Gửi `GET /api/stock-movements` | Header Session | HTTP 200 OK. Danh sách phiếu sắp xếp giảm dần theo thời gian. | **PASS** |
| **TC_STOCK_02** | Tạo phiếu Nhập kho (Dương) | Positive | 1. Gửi `POST /api/stock-movements`<br>2. Kiểm tra tồn kho sản phẩm tăng | `productId: 1`<br>`quantityChange: 30` | HTTP 201 Created. Tồn kho sản phẩm 1 tăng thêm 30 đơn vị. Ghi nhận người tạo. | **PASS** |
| **TC_STOCK_03** | Tạo phiếu Xuất kho (Âm) | Positive | 1. Gửi `POST /api/stock-movements`<br>2. Kiểm tra tồn kho sản phẩm giảm | `productId: 1`<br>`quantityChange: -10` | HTTP 201 Created. Tồn kho sản phẩm 1 giảm đi đúng 10 đơn vị. | **PASS** |
| **TC_STOCK_04** | Chống xuất âm kho (Insufficient Stock) | Data Integrity | 1. Xuất số lượng lớn hơn tồn kho hiện tại | `productId: 1`<br>`quantityChange: -99999` | HTTP 400 Bad Request. Báo `"Insufficient stock in warehouse! Currently only has X units available..."`. Tồn kho không đổi. | **PASS** |
| **TC_STOCK_05** | Tạo phiếu với số lượng bằng 0 | Negative | 1. Gửi `POST /api/stock-movements` với số lượng 0 | `quantityChange: 0` | HTTP 400 Bad Request. Báo `"Quantity change cannot be zero!"`. | **PASS** |
| **TC_STOCK_06** | Tạo phiếu với số lượng tràn số nguyên | Negative | 1. Nhập số lượng vượt 1.000.000 đơn vị | `quantityChange: 5000000` | HTTP 400 Bad Request. Báo `"Quantity change cannot exceed 1,000,000 units..."`. | **PASS** |
| **TC_STOCK_07** | Xóa phiếu kho và hoàn tác tồn kho | Data Integrity | 1. Xóa phiếu nhập 20 cái<br>2. Kiểm tra tồn kho | `DELETE /api/stock-movements/{id}` | HTTP 200 OK. Tồn kho sản phẩm tự động giảm 20 cái. Phiếu được xóa mềm (`is_deleted = 1`). | **PASS** |
| **TC_STOCK_08** | Chặn xóa phiếu kho nếu làm âm tồn kho | Data Integrity | 1. Nhập 10 cái, đã xuất bán 8 cái (còn 2)<br>2. Thử xóa phiếu nhập 10 cái ban đầu | `DELETE /api/stock-movements/{id_nhap}` | HTTP 400 Bad Request. Báo `"Cannot delete this stock movement because it would cause warehouse stock... to become negative"`. Không xóa phiếu. | **PASS** |
| **TC_STOCK_09** | Role USER thử tạo phiếu kho | Security | 1. Đăng nhập tài khoản `USER`<br>2. Thử gọi API tạo phiếu | Role USER | HTTP 403 Forbidden. Từ chối quyền ghi dữ liệu. | **PASS** |
| **TC_STOCK_10** | Chặn biến động kho với sản phẩm SUSPENDED | Negative | 1. Sản phẩm đang có status = `SUSPENDED`<br>2. Thử tạo phiếu kho (nhập hoặc xuất) | `productId` của sản phẩm Suspended<br>`quantityChange: 10` | HTTP 400 Bad Request. Báo `"Cannot perform stock movement on a suspended product! Please reactivate the product first."`. Không tạo phiếu kho. | **PASS** |

---

### PHÂN HỆ 5: QUẢN TRỊ NGƯỜI DÙNG & BẢO VỆ ADMIN (USER MANAGEMENT)

| Mã Test Case | Tên Chức Năng | Phân Loại | Các Bước Thực Hiện | Dữ Liệu Đầu Vào | Kết Quả Mong Đợi | Trạng Thái |
| :--- | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_USER_01** | Admin xem danh sách người dùng | Positive | 1. Đăng nhập Admin<br>2. Gửi `GET /api/users` | Admin session | HTTP 200 OK. Trả về toàn bộ tài khoản đang hoạt động. | **PASS** |
| **TC_USER_02** | Staff hoặc User xem danh sách user | Security | 1. Đăng nhập Staff hoặc User<br>2. Gửi `GET /api/users` | Staff / User session | HTTP 403 Forbidden. Báo `"Access denied: Administrator privilege required"`. | **PASS** |
| **TC_USER_03** | Phân quyền nâng tài khoản lên STAFF | Positive | 1. Admin gọi `PUT /api/users/{id}/role` | `role: "STAFF"` | HTTP 200 OK. Tài khoản được cấp quyền nhân viên kho ngay lập tức. | **PASS** |
| **TC_USER_04** | Gán vai trò không hợp lệ | Negative | 1. Nhập role không tồn tại | `role: "MANAGER_VIP"` | HTTP 400 Bad Request. Báo `"Invalid role. Permitted roles: ADMIN, STAFF, USER"`. | **PASS** |
| **TC_USER_05** | Chặn hạ quyền Admin duy nhất | Data Integrity | 1. Hạ quyền của Admin duy nhất trong DB | `role: "USER"` | HTTP 400 Bad Request. Báo `"Cannot demote the last remaining Administrator in the system"`. | **PASS** |
| **TC_USER_06** | Admin xóa tài khoản nhân viên (Xóa mềm) | Positive | 1. Admin bấm xóa nhân viên | `DELETE /api/users/{staff_id}` | HTTP 200 OK. Báo xóa thành công. Trong DB: `is_deleted = 1`. Tài khoản bị vô hiệu hóa đăng nhập. | **PASS** |
| **TC_USER_07** | Admin tự xóa chính tài khoản mình | Security | 1. Admin gọi xóa ID của chính mình | `DELETE /api/users/{current_admin_id}` | HTTP 400 Bad Request. Báo `"You cannot delete your own logged-in account"`. | **PASS** |

---

### PHÂN HỆ 6: BẢO MẬT & XỬ LÝ NGOẠI LỆ TOÀN CỤC (GLOBAL EXCEPTION & SECURITY)

| Mã Test Case | Tên Chức Năng | Phân Loại | Các Bước Thực Hiện | Dữ Liệu Đầu Vào | Kết Quả Mong Đợi | Trạng Thái |
| :--- | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_SEC_01** | Xử lý JSON sai cú pháp (Malformed JSON) | Negative | 1. Gửi body JSON bị thiếu dấu ngoặc nhọn | `{"productId": 1` | HTTP 400 Bad Request. Trả về JSON chuẩn thông báo `"Malformed JSON request..."` thay vì Whitelabel HTML. | **PASS** |
| **TC_SEC_02** | Chống tấn công XSS trong trường dữ liệu | Security | 1. Thêm sản phẩm chứa script mã độc | `productName: "<script>alert(1)</script>"` | Lưu an toàn vào DB. Giao diện frontend dùng hàm `escapeHtml()` vô hiệu hóa script thành văn bản thuần. | **PASS** |
| **TC_SEC_03** | Bảo vệ mật khẩu chuẩn PBKDF2 | Security | 1. Kiểm tra cột `password` trong bảng `users` | SQL Query | Mật khẩu lưu dưới dạng `Salt:Hash` băm 65.536 vòng, hoàn toàn không lưu bản rõ (plaintext). | **PASS** |
