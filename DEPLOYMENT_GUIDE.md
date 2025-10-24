# 🚀 Hướng Dẫn Deploy EVRental Backend Lên Server

> **Dành cho người mới bắt đầu - Hướng dẫn từng bước chi tiết**

## 📋 Mục Lục
1. [Chuẩn bị](#1-chuẩn-bị)
2. [Thuê và setup VPS](#2-thuê-và-setup-vps)
3. [Cài đặt Docker trên server](#3-cài-đặt-docker-trên-server)
4. [Setup GitHub Secrets](#4-setup-github-secrets)
5. [Cấu hình server](#5-cấu-hình-server)
6. [Deploy lần đầu](#6-deploy-lần-đầu)
7. [Kiểm tra và monitoring](#7-kiểm-tra-và-monitoring)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. Chuẩn Bị

### ✅ Checklist trước khi bắt đầu:

- [ ] Tài khoản GitHub (đã có)
- [ ] Tài khoản Docker Hub (miễn phí)
- [ ] VPS/Server (khuyến nghị: DigitalOcean, Vultr, hoặc AWS)
- [ ] Domain (tùy chọn, có thể dùng IP)

### 💰 Chi phí ước tính:

- **VPS**: $5-10/tháng (2GB RAM, 1 CPU)
- **Domain**: $10-15/năm (tùy chọn)
- **Docker Hub**: Miễn phí
- **GitHub Actions**: Miễn phí (2000 phút/tháng)

---

## 2. Thuê và Setup VPS

### Option 1: DigitalOcean (Khuyến nghị cho người mới)

1. **Đăng ký tài khoản:**
   - Truy cập: https://www.digitalocean.com
   - Đăng ký với email
   - Nhận $200 credit miễn phí (60 ngày)

2. **Tạo Droplet (VPS):**
   ```
   Click "Create" → "Droplets"
   
   Choose an image:
   ✅ Ubuntu 22.04 LTS x64
   
   Choose size:
   ✅ Basic Plan - $6/month
      - 1 CPU
      - 2GB RAM
      - 50GB SSD
   
   Choose datacenter region:
   ✅ Singapore (gần Việt Nam nhất)
   
   Authentication:
   ✅ SSH Key (AN TOÀN HƠN) - Xem bước 2.1
   hoặc
   ⚠️ Password (đơn giản hơn)
   
   Hostname:
   ✅ evrental-backend
   
   Click "Create Droplet"
   ```

3. **Lưu lại thông tin:**
   ```
   IP Address: 123.456.789.10 (ví dụ)
   Username: root
   Password/SSH Key: (đã tạo ở bước trên)
   ```

### 2.1. Tạo SSH Key (QUAN TRỌNG - AN TOÀN)

**Windows (Git Bash):**
```bash
# Mở Git Bash
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"

# Nhấn Enter 3 lần (để mặc định)
# File sẽ được tạo tại: C:\Users\YourName\.ssh\id_rsa

# Xem nội dung public key
cat ~/.ssh/id_rsa.pub

# Copy toàn bộ nội dung (bắt đầu với ssh-rsa...)
```

**Paste vào DigitalOcean khi tạo Droplet:**
- Click "New SSH Key"
- Paste nội dung `id_rsa.pub`
- Đặt tên: "My Laptop"

### Option 2: Vultr (Rẻ hơn)

Similar steps, giá từ $3.5/tháng

### Option 3: AWS EC2 (Phức tạp hơn nhưng mạnh)

Free tier 12 tháng đầu (giới hạn 750 giờ/tháng)

---

## 3. Cài Đặt Docker Trên Server

### 3.1. Kết nối vào server

**Windows (Git Bash hoặc PowerShell):**
```bash
# Nếu dùng SSH Key
ssh root@123.456.789.10

# Nếu dùng Password
ssh root@123.456.789.10
# Nhập password khi được hỏi

# Lần đầu sẽ hỏi "Are you sure...?" → gõ "yes"
```

### 3.2. Update hệ thống

```bash
# Update package list
sudo apt update && sudo apt upgrade -y

# Cài đặt các package cần thiết
sudo apt install -y curl git wget nano
```

### 3.3. Cài đặt Docker

```bash
# Xóa Docker cũ (nếu có)
sudo apt remove docker docker-engine docker.io containerd runc

# Cài đặt Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Thêm user vào docker group (không cần sudo)
sudo usermod -aG docker $USER

# Khởi động Docker
sudo systemctl enable docker
sudo systemctl start docker

# Kiểm tra
docker --version
# Kết quả: Docker version 24.x.x
```

### 3.4. Cài đặt Docker Compose

```bash
# Cài đặt Docker Compose
sudo apt install -y docker-compose-plugin

# Kiểm tra
docker compose version
# Kết quả: Docker Compose version v2.x.x
```

### 3.5. Kiểm tra cài đặt

```bash
# Test Docker
docker run hello-world

# Nếu thấy "Hello from Docker!" → SUCCESS! ✅
```

---

## 4. Setup GitHub Secrets

### 4.1. Tạo Docker Hub Token

1. Đăng nhập https://hub.docker.com
2. Click avatar → **Account Settings**
3. **Security** → **New Access Token**
4. Token description: `GitHub Actions`
5. Access permissions: **Read, Write, Delete**
6. Click **Generate**
7. **COPY TOKEN NGAY** (chỉ hiện 1 lần!)

### 4.2. Tạo SSH Key cho GitHub Actions

**Trên server (đã SSH vào):**
```bash
# Tạo SSH key mới cho GitHub Actions
ssh-keygen -t rsa -b 4096 -f ~/.ssh/github_actions_key

# Nhấn Enter 2 lần (không cần passphrase)

# Thêm public key vào authorized_keys
cat ~/.ssh/github_actions_key.pub >> ~/.ssh/authorized_keys

# Xem private key (COPY toàn bộ)
cat ~/.ssh/github_actions_key
# Kết quả:
# -----BEGIN OPENSSH PRIVATE KEY-----
# b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAACFwAAAAdzc2gtcn
# ... (nhiều dòng)
# -----END OPENSSH PRIVATE KEY-----
```

### 4.3. Thêm Secrets vào GitHub

1. Mở repository: https://github.com/cuong78/evRentalBE
2. **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**

**Thêm từng secret sau:**

| Secret Name | Value | Ví dụ |
|------------|-------|-------|
| `DOCKER_HUB_USERNAME` | Username Docker Hub của bạn | `anhcuong8386` |
| `DOCKER_HUB_ACCESS_TOKEN` | Token vừa tạo ở bước 4.1 | `dckr_pat_xxxxx...` |
| `SERVER_HOST` | IP của VPS | `123.456.789.10` |
| `SERVER_USER` | Username SSH | `root` |
| `SERVER_SSH_KEY` | Private key từ bước 4.2 | `-----BEGIN OPENSSH...` |
| `SERVER_PORT` | Port SSH (mặc định) | `22` |

**Screenshot hướng dẫn:**
```
GitHub Repo → Settings → Secrets and variables → Actions
→ New repository secret
→ Name: DOCKER_HUB_USERNAME
→ Secret: anhcuong8386
→ Add secret
```

---

## 5. Cấu Hình Server

### 5.1. Tạo cấu trúc thư mục

```bash
# SSH vào server
ssh root@YOUR_SERVER_IP

# Tạo thư mục dự án
sudo mkdir -p /opt/evrental
cd /opt/evrental

# Set quyền
sudo chown -R $USER:$USER /opt/evrental
```

### 5.2. Tạo file .env

```bash
# Tạo file .env
nano /opt/evrental/.env
```

**Paste nội dung sau (CHÚ Ý: THAY ĐỔI CÁC GIÁ TRỊ):**

```env
# ==================== DATABASE ====================
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/evrental
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=ChangeMe_SecurePassword123!

POSTGRES_DB=evrental
POSTGRES_USER=postgres
POSTGRES_PASSWORD=ChangeMe_SecurePassword123!

# ==================== JWT ====================
JWT_SECRET=ChangeMe_YourSuperSecretJWTKey_MinLength32Characters_Random123!
JWT_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=86400000

# ==================== EMAIL ====================
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password-here

# ==================== FRONTEND URLs ====================
# Thay YOUR_DOMAIN bằng domain/IP thực tế
FRONTEND_URL_BASE=http://YOUR_DOMAIN:3000
FRONTEND_URL_PAYMENT_RETURN=http://YOUR_DOMAIN:3000/payment/vnpay-return
FRONTEND_URL_EMAIL_VERIFICATION=http://YOUR_DOMAIN:3000/verify-email

# ==================== VNPAY ====================
PAYMENT_VNPAY_TMN_CODE=F9EZ1X6Z
PAYMENT_VNPAY_SECRET_KEY=XV3C5IN6HACS0NWN5OHG1IDLYOZWI1VX
PAYMENT_VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
PAYMENT_VNPAY_IP_ADDRESS=127.0.0.1
PAYMENT_VNPAY_WALLET_RETURN_URL=http://YOUR_DOMAIN:8080/api/v1/payment/wallet/return

# ==================== CLOUDINARY ====================
CLOUDINARY_CLOUD_NAME=duklfdbqf
CLOUDINARY_API_KEY=896897124846695
CLOUDINARY_API_SECRET=OPLZ7KB09RIZZXJlN7Im8w75w9k

# ==================== SERVER ====================
SERVER_PORT=8080
TZ=Asia/Ho_Chi_Minh
```

**Lưu file:**
- Nhấn `Ctrl + X`
- Nhấn `Y`
- Nhấn `Enter`

### 5.3. Bảo mật file .env

```bash
# Chỉ owner mới đọc được
chmod 600 /opt/evrental/.env

# Kiểm tra
ls -la /opt/evrental/.env
# Kết quả: -rw------- 1 root root ... .env
```

### 5.4. Tạo file docker-compose.yml tạm thời

```bash
# Tạo file docker-compose.yml (sẽ được GitHub Actions overwrite)
nano /opt/evrental/docker-compose.yml
```

**Paste nội dung:**
```yaml
version: '3.8'

services:
  postgres:
    container_name: evrental-postgres
    image: postgres:17.5
    restart: unless-stopped
    env_file: .env
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - evrental-network

  app:
    container_name: evrental-app
    image: anhcuong8386/evrental-backend:latest
    restart: unless-stopped
    ports:
      - "8080:8080"
    env_file: .env
    depends_on:
      - postgres
    networks:
      - evrental-network

networks:
  evrental-network:
    driver: bridge

volumes:
  postgres-data:
    driver: local
```

**Lưu file:** `Ctrl + X` → `Y` → `Enter`

---

## 6. Deploy Lần Đầu

### 6.1. Cập nhật docker-compose.yml trong dự án

**Sửa file `docker-compose.yml` trong dự án:**

```yaml
version: '3.8'

services:
  postgres:
    container_name: evrental-postgres
    image: postgres:17.5
    restart: unless-stopped
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
      TZ: Asia/Ho_Chi_Minh
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - evrental-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d evrental"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    container_name: evrental-app
    # build:              # ← Comment out khi deploy
    #   context: .
    #   dockerfile: Dockerfile
    image: anhcuong8386/evrental-backend:latest  # ← Thêm dòng này
    restart: unless-stopped
    ports:
      - "8080:8080"
    env_file:
      - .env
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - evrental-network

networks:
  evrental-network:
    driver: bridge

volumes:
  postgres-data:
    driver: local
```

### 6.2. Push code lên GitHub

```bash
# Trên máy local
git add .
git commit -m "Setup CI/CD pipeline"
git push origin main
```

### 6.3. Theo dõi deployment

1. Mở GitHub: https://github.com/cuong78/evRentalBE
2. Click tab **Actions**
3. Xem workflow đang chạy
4. Click vào workflow để xem chi tiết từng bước

**Thời gian ước tính:**
- Build & Test: ~3-5 phút
- Docker Build & Push: ~5-7 phút
- Deploy to Server: ~2-3 phút
- **Tổng: ~10-15 phút**

### 6.4. Kiểm tra trên server

```bash
# SSH vào server
ssh root@YOUR_SERVER_IP

# Kiểm tra containers
cd /opt/evrental
docker compose ps

# Kết quả mong đợi:
# NAME                 STATUS              PORTS
# evrental-postgres    Up 5 minutes        0.0.0.0:5432->5432/tcp
# evrental-app         Up 2 minutes        0.0.0.0:8080->8080/tcp

# Xem logs
docker compose logs -f app

# Nhấn Ctrl+C để thoát
```

---

## 7. Kiểm Tra và Monitoring

### 7.1. Test API

```bash
# Test health check
curl http://YOUR_SERVER_IP:8080/actuator/health

# Kết quả mong đợi:
# {"status":"UP"}

# Test API endpoint (ví dụ)
curl http://YOUR_SERVER_IP:8080/api/v1/vehicles

# Hoặc mở trình duyệt:
http://YOUR_SERVER_IP:8080/actuator/health
```

### 7.2. Xem logs real-time

```bash
# Logs của app
docker compose logs -f app

# Logs của database
docker compose logs -f postgres

# Logs của tất cả services
docker compose logs -f
```

### 7.3. Monitoring script

Tạo script để check health:

```bash
nano /opt/evrental/health-check.sh
```

Paste:
```bash
#!/bin/bash

echo "🏥 EVRental Backend Health Check"
echo "================================="

# Check containers
echo ""
echo "📦 Container Status:"
docker compose ps

# Check health endpoint
echo ""
echo "🔍 Health Endpoint:"
curl -s http://localhost:8080/actuator/health | jq '.'

# Check disk space
echo ""
echo "💾 Disk Space:"
df -h /

# Check memory
echo ""
echo "🧠 Memory Usage:"
free -h

# Check recent logs
echo ""
echo "📋 Recent Logs (last 20 lines):"
docker compose logs --tail=20 app
```

Chạy:
```bash
chmod +x /opt/evrental/health-check.sh
./health-check.sh
```

---

## 8. Troubleshooting

### ❌ Container không start

```bash
# Xem logs chi tiết
docker compose logs app

# Restart container
docker compose restart app

# Nếu vẫn lỗi, rebuild
docker compose down
docker compose pull
docker compose up -d
```

### ❌ Không connect được database

```bash
# Kiểm tra database container
docker compose logs postgres

# Test connection từ app container
docker compose exec app bash
# Trong container:
apt update && apt install postgresql-client -y
psql -h postgres -U postgres -d evrental
# Nhập password từ .env
```

### ❌ Port 8080 không truy cập được

```bash
# Kiểm tra firewall
sudo ufw status

# Mở port 8080 (nếu firewall đang bật)
sudo ufw allow 8080/tcp
sudo ufw reload

# Kiểm tra app có listen không
netstat -tulpn | grep 8080
```

### ❌ GitHub Actions failed

**Kiểm tra:**
1. GitHub Secrets đã đúng chưa?
2. Server SSH có hoạt động không? (`ssh root@YOUR_IP`)
3. Xem logs chi tiết trong tab Actions

### ❌ Out of memory

```bash
# Kiểm tra memory
free -h

# Tạo swap file (nếu RAM < 2GB)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Tự động mount swap khi reboot
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

## 🎯 Workflow Sau Khi Setup Xong

### Khi có code mới:

```bash
# 1. Code trên local
# ... làm việc với code ...

# 2. Commit và push
git add .
git commit -m "Add new feature"
git push origin main

# 3. GitHub Actions tự động:
#    ✅ Build & test
#    ✅ Build Docker image
#    ✅ Push to Docker Hub
#    ✅ Deploy to server
#    ✅ Health check

# 4. Kiểm tra (sau 10-15 phút)
curl http://YOUR_SERVER_IP:8080/actuator/health
```

**XONG! Không cần làm gì thêm! 🎉**

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:

1. **Kiểm tra logs GitHub Actions** (tab Actions)
2. **Kiểm tra logs trên server** (`docker compose logs`)
3. **Chạy health-check script**
4. **Xem phần Troubleshooting ở trên**

---

## 🔗 Tài Liệu Tham Khảo

- [Docker Documentation](https://docs.docker.com)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [DigitalOcean Tutorials](https://www.digitalocean.com/community/tutorials)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)

---

**Good luck! 🚀**
