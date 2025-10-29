# Environment Variables Setup

## Required Environment Variables

Để chạy AccountService, bạn cần thiết lập các biến môi trường sau:

### 1. Database Configuration
```
DB_URL=jdbc:sqlserver://LAPTOP-HSE5F4RV\\DGHIEU:1433;databaseName=LabVerseDB;encrypt=false
DB_USERNAME=sa
DB_PASSWORD=123
```

### 2. JWT Configuration
```
JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
JWT_EXPIRATION=86400000
```

### 3. Google OAuth2 Configuration
```
GOOGLE_CLIENT_ID=536556216607-m4aofut1aco3qb73e1tvrkijkl5a7s3k.apps.googleusercontent.com
```

### 4. Email Configuration (Gmail SMTP)
```
MAIL_USERNAME=hieuduong2524@gmail.com
MAIL_PASSWORD=qdep xamw xzwa wppx
```

## Cách thiết lập

### Option 1: Sử dụng IDE (IntelliJ IDEA / Eclipse)
1. Mở Run/Debug Configurations
2. Thêm các Environment Variables vào mục "Environment variables"
3. Format: `KEY=VALUE;KEY2=VALUE2`

### Option 2: Sử dụng file .env (với spring-dotenv)
1. Thêm dependency vào `pom.xml`:
```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```
2. Tạo file `.env` trong thư mục root của AccountService
3. Copy các environment variables ở trên vào file `.env`
4. File `.env` sẽ tự động được load khi chạy ứng dụng

### Option 3: Sử dụng System Environment Variables (Production)
1. Windows:
   - Mở System Properties > Advanced > Environment Variables
   - Thêm các biến vào User variables hoặc System variables

2. Linux/Mac:
   ```bash
   export DB_URL="jdbc:sqlserver://..."
   export DB_USERNAME="sa"
   export DB_PASSWORD="123"
   # ... các biến khác
   ```

3. Hoặc thêm vào file `~/.bashrc` hoặc `~/.zshrc` để lưu vĩnh viễn

### Option 4: Docker/Docker Compose
Thêm vào `docker-compose.yml`:
```yaml
services:
  account-service:
    environment:
      - DB_URL=jdbc:sqlserver://...
      - DB_USERNAME=sa
      - DB_PASSWORD=123
      - JWT_SECRET=...
      - GOOGLE_CLIENT_ID=...
      - MAIL_USERNAME=...
      - MAIL_PASSWORD=...
```

## Lưu ý bảo mật
⚠️ **QUAN TRỌNG**: 
- KHÔNG commit file `.env` hoặc file chứa thông tin nhạy cảm vào Git
- Đảm bảo `.env` đã được thêm vào `.gitignore`
- Sử dụng secrets management trong production (AWS Secrets Manager, Azure Key Vault, etc.)

