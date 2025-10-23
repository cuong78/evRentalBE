# 📝 Quick Start - Deploy EVRental Backend

> **TL;DR - Các bước tóm tắt nhanh**

## ⚡ Chuẩn bị (5 phút)

### 1. Tạo tài khoản cần thiết
- ✅ Docker Hub: https://hub.docker.com (miễn phí)
- ✅ VPS: DigitalOcean/Vultr ($5-6/tháng)

### 2. Thuê VPS
```
Ubuntu 22.04 LTS
2GB RAM, 1 CPU, 50GB SSD
Region: Singapore
Cost: ~$6/month
```

## 🔧 Setup Server (10 phút)

```bash
# 1. SSH vào server
ssh root@YOUR_SERVER_IP

# 2. Cài Docker (copy-paste cả đoạn)
curl -fsSL https://get.docker.com -o get-docker.sh && \
sudo sh get-docker.sh && \
sudo apt install -y docker-compose-plugin && \
docker --version

# 3. Tạo thư mục dự án
mkdir -p /opt/evrental && cd /opt/evrental

# 4. Tạo file .env
nano .env
```

**Paste vào file .env (THAY ĐỔI PASSWORD):**
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/evrental
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=YourSecurePassword123!

POSTGRES_DB=evrental
POSTGRES_USER=postgres
POSTGRES_PASSWORD=YourSecurePassword123!

JWT_SECRET=YourSuperSecretJWTKeyMinLength32CharsRandom!
JWT_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=86400000

SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

FRONTEND_URL_BASE=http://YOUR_SERVER_IP:3000
FRONTEND_URL_PAYMENT_RETURN=http://YOUR_SERVER_IP:3000/payment/vnpay-return
FRONTEND_URL_EMAIL_VERIFICATION=http://YOUR_SERVER_IP:3000/verify-email

PAYMENT_VNPAY_TMN_CODE=F9EZ1X6Z
PAYMENT_VNPAY_SECRET_KEY=XV3C5IN6HACS0NWN5OHG1IDLYOZWI1VX
PAYMENT_VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
PAYMENT_VNPAY_IP_ADDRESS=127.0.0.1
PAYMENT_VNPAY_WALLET_RETURN_URL=http://YOUR_SERVER_IP:8080/api/v1/payment/wallet/return

CLOUDINARY_CLOUD_NAME=duklfdbqf
CLOUDINARY_API_KEY=896897124846695
CLOUDINARY_API_SECRET=OPLZ7KB09RIZZXJlN7Im8w75w9k

SERVER_PORT=8080
TZ=Asia/Ho_Chi_Minh
```

**Lưu:** `Ctrl + X` → `Y` → `Enter`

```bash
# 5. Bảo mật file
chmod 600 .env

# 6. Tạo SSH key cho GitHub Actions
ssh-keygen -t rsa -b 4096 -f ~/.ssh/github_actions_key
# Nhấn Enter 2 lần

# 7. Thêm key vào authorized_keys
cat ~/.ssh/github_actions_key.pub >> ~/.ssh/authorized_keys

# 8. Copy private key (dùng cho GitHub Secrets)
cat ~/.ssh/github_actions_key
# Copy toàn bộ từ -----BEGIN đến -----END-----
```

## 🔑 Setup GitHub Secrets (5 phút)

### 1. Tạo Docker Hub Token
- Đăng nhập https://hub.docker.com
- Account Settings → Security → New Access Token
- Copy token

### 2. Thêm vào GitHub
Repository → Settings → Secrets and variables → Actions

| Secret Name | Value |
|------------|-------|
| `DOCKER_HUB_USERNAME` | `anhcuong8386` |
| `DOCKER_HUB_ACCESS_TOKEN` | Token vừa tạo |
| `SERVER_HOST` | IP server của bạn |
| `SERVER_USER` | `root` |
| `SERVER_SSH_KEY` | Private key từ bước 8 trên |
| `SERVER_PORT` | `22` |

## 🚀 Deploy (2 phút)

```bash
# Trên máy local
git add .
git commit -m "Setup CI/CD"
git push origin main
```

**→ Vào GitHub → tab Actions → Xem deployment!**

## ✅ Kiểm tra (1 phút)

```bash
# Test API
curl http://YOUR_SERVER_IP:8080/actuator/health

# Kết quả: {"status":"UP"}
```

## 🎯 Workflow sau này

```bash
# 1. Code
# 2. git push origin main
# 3. ĐỢI 10-15 phút
# 4. XONG! ✅
```

---

## 📞 Troubleshooting Nhanh

### Container không start?
```bash
ssh root@YOUR_SERVER_IP
cd /opt/evrental
docker compose logs app
docker compose restart app
```

### Port 8080 không truy cập được?
```bash
sudo ufw allow 8080/tcp
sudo ufw reload
```

### GitHub Actions failed?
- Kiểm tra Secrets đã đúng chưa
- SSH vào server thử: `ssh root@YOUR_SERVER_IP`

---

## 📚 Chi tiết đầy đủ

Xem file `DEPLOYMENT_GUIDE.md` để có hướng dẫn chi tiết từng bước!

---

**Total time: ~25 phút** ⏱️

**Cost: ~$6/tháng** 💰

**Difficulty: ⭐⭐ (Easy)** 🎉
