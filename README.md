# **iShop** - _Hệ thống cửa hàng thông minh_

> _`iShop` là một hệ thống quản lý cửa hàng thông minh, cho phép người dùng dễ dàng theo dõi và quản lý sản phẩm, đơn hàng và khách hàng._

## **`1.` Thành viên**:

| **Tên**            | **MSSV**       | **Note**      |
| ------------------ | -------------- | ------------- |
| **Nguyễn Đức Đạt** | **B22DCCN195** |               |
| **Phạm Văn Đức**   | **B22DCCN243** | _Trưởng nhóm_ |
| **Trần Gia Hiển**  | **B22DCCN291** |               |
| **Chu Ngọc Thắng** | **B22DCCN807** |               |

## **`2.` Các tính năng**:

### Phía **Khách hàng**:

> _không yêu cầu đăng nhập_

- Xem danh sách sản phẩm, thêm giỏ hàng.
- Đặt hàng: (_có 2 cách đặt hàng_)
  - **Tự đặt hàng**: Khách hàng tự chọn, thêm sản phẩm và đặt hàng không cần giao tiếp với nhân viên.
  - **Đặt hàng qua nhân viên**: Khách hàng giao tiếp với nhân viên, nhân viên sẽ giúp khách hàng chọn sản phẩm và đặt hàng.
- Theo dõi đơn hàng qua _Mã đơn hàng_
- Thanh toán đơn hàng qua nhân viên.

### Phía **Nhân viên**:

> _yêu cầu đăng nhập tài khoản người dùng với `role=STAFF`_

- Đặt hàng cho khách.
- Cho khách hàng thanh toán.
- Quản lý trạng thái đơn hàng.
- Quản lý tài khoản các nhân.

### Phía **Quản lý**:

> _yêu cầu đăng nhập tài khoản người dùng với `role=MANAGER`_

- Quản lý nhân viên:
  - Thêm, sửa, xóa tài khoản nhân viên.
  - Reset password cho nhân viên.
- Quản lý tài khoản cá nhân
- Quản lý danh mục sản phẩm.
- Quản lý sản phẩm:
  - Quản lý thông tin sản phẩm.
  - Quản lý trạng thái sản phẩm (đang bán/dừng bán).
- Quản lý giảm giá sản phẩm.
- Xem thống kê theo: doanh thu, danh mục, sản phẩm, nhân viên, ...

## **`3.` Cách chạy dự án**:

### 3.1. Chạy Backend (Spring Boot)

- Mở `cmd`, di chuyển tới thư mục backend::

  ```cmd
  cd backend/app-be
  ```

- Khởi tạo `.env`:

  ```cmd
  copy .env-example .env
  ```

  > _Sau khi copy, mở `.env` và gán các tham số DB local_

- Chạy backend:

  ```cmd
  ./gradlew.bat bootRun
  ```

Backend mặc định chạy ở cổng `3333`.

### 3.2. Khởi tạo tài khoản "**MANAGER**" (**`Nếu chạy lần đầu`**)

Tạo tài khoản MANAGER (`cmd`):

```cmd
curl -X POST "http://localhost:3333/api/v1/users/no-auth" ^
-H "Content-Type: application/json" ^
-d "{\"fullName\":\"Admin\",\"email\":\"manager@ishop.com\",\"role\":\"MANAGER\"}"
```

Mật khẩu ban đầu là `DEFAULT_PASSWORD` (mặc định: `12345678`, có thể cấu hình trong env).

### 3.3. Chạy App Android

Cấu hình connect tới Backend tại: [Constants.java](app/src/app/src/main/java/com/example/ddht/utils/Constants.java)

Chạy App:

- Mở project Android ở thư mục `app/src` bằng Android Studio.
- Chọn thiết bị Emulator.
- Run module `app`.

## **`4.` Công nghệ sử dụng**:

- **Backend**: Java (SpringBoot), WebSocket, Cloudinary.
- **App**: Java (Android)
- **Database**: MySQL

## **`5.` Kết quả đạt được**

- Xây dựng được backend Spring Boot phục vụ API quản lý sản phẩm, danh mục, saleoff, đơn hàng, thanh toán và thống kê.
- Hoàn thiện ứng dụng Android cho cả 3 nhóm người dùng: khách hàng, nhân viên và quản lý.
- Thiết lập phân quyền theo vai trò `MANAGER` và `STAFF`, đảm bảo tách biệt chức năng theo từng nhóm tài khoản.
- Tích hợp kết nối app -> backend ổn định qua base URL local dành cho Android Emulator.
