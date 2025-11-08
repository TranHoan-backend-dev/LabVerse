# Hướng dẫn Setup Biến Môi Trường

## Vấn đề
Lỗi `supabaseUrl is required` xảy ra vì thiếu file `.env` với các biến môi trường Supabase.

## Giải pháp

### Bước 1: Tạo file `.env` trong thư mục `web/`

Tạo file `.env` với nội dung sau:

```env
# Supabase Configuration
VITE_SUPABASE_URL=http://localhost:54321
VITE_SUPABASE_PUBLISHABLE_KEY=your-anon-key-here
```

### Bước 2: Lấy Supabase URL và Key

#### ⚠️ Lưu ý: Supabase Local yêu cầu Docker Desktop

Nếu bạn gặp lỗi `Docker Desktop is a prerequisite`, bạn có 2 lựa chọn:

**Lựa chọn 1: Cài đặt Docker Desktop (cho Supabase Local)**
- Tải Docker Desktop: https://docs.docker.com/desktop/install/windows-install/
- Cài đặt và khởi động Docker Desktop
- Đảm bảo Docker Desktop đang chạy (icon Docker ở system tray)
- Sau đó chạy lại: `npx supabase start`

**Lựa chọn 2: Sử dụng Supabase Cloud (Khuyến nghị - Dễ hơn)**
- Không cần Docker Desktop
- Xem hướng dẫn ở phần "Nếu bạn đang dùng Supabase Cloud" bên dưới

#### Nếu bạn đang dùng Supabase Local (yêu cầu Docker Desktop):

1. **Khởi động Supabase local:**
   ```bash
   cd web
   npx supabase start
   ```

2. **Lấy thông tin từ output**, bạn sẽ thấy:
   ```
   API URL: http://localhost:54321
   anon key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

3. **Cập nhật file `.env`:**
   ```env
   VITE_SUPABASE_URL=http://localhost:54321
   VITE_SUPABASE_PUBLISHABLE_KEY=<anon-key-từ-output>
   ```

#### Nếu bạn đang dùng Supabase Cloud (Khuyến nghị - Không cần Docker):

1. **Tạo tài khoản Supabase** (nếu chưa có):
   - Vào https://app.supabase.com
   - Đăng ký/Đăng nhập bằng GitHub hoặc Email

2. **Tạo project mới:**
   - Click "New Project"
   - Điền thông tin (tên project, database password, region)
   - Chờ project được tạo (khoảng 2 phút)

3. **Lấy thông tin API:**
   - Vào project vừa tạo
   - Click **Settings** (biểu tượng bánh răng) ở sidebar trái
   - Chọn **API** trong menu Settings
   - Copy các giá trị sau:
     - **Project URL** → Đây là `VITE_SUPABASE_URL`
     - **anon public** key → Đây là `VITE_SUPABASE_PUBLISHABLE_KEY`

4. **Cập nhật file `.env`:**
   ```env
   VITE_SUPABASE_URL=https://xxxxx.supabase.co
   VITE_SUPABASE_PUBLISHABLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

   **Ví dụ thực tế:**
   ```env
   VITE_SUPABASE_URL=https://abcdefghijklmnop.supabase.co
   VITE_SUPABASE_PUBLISHABLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFiY2RlZmdoaWprbG1ub3AiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTYzODk2NzI5MCwiZXhwIjoxOTU0NTQzMjkwfQ.example
   ```

5. **Lưu file `.env` và khởi động lại dev server**

### Bước 3: Khởi động lại dev server

Sau khi tạo file `.env`, bạn cần khởi động lại Vite dev server:

```bash
# Dừng server hiện tại (Ctrl+C)
# Sau đó chạy lại:
npm run dev
```

## Lưu ý

- File `.env` đã được thêm vào `.gitignore` nên sẽ không bị commit lên git
- Không commit file `.env` chứa credentials thật lên repository
- Nếu bạn muốn chia sẻ cấu trúc, có thể tạo file `.env.example` (không chứa giá trị thật)

## Kiểm tra

Sau khi setup xong, mở browser console và kiểm tra:
- Không còn lỗi `supabaseUrl is required`
- Ứng dụng load được bình thường

