# ✅ Deployment Checklist - EVRental Backend

> **In ra và đánh dấu từng bước khi hoàn thành**

## 📋 Phase 1: Chuẩn Bị (Thời gian: 5-10 phút)

### Tài Khoản

- [ ] Tạo tài khoản Docker Hub (https://hub.docker.com)
- [ ] Verify email Docker Hub
- [ ] Chọn nhà cung cấp VPS:
  - [ ] DigitalOcean (khuyến nghị) - $200 credit
  - [ ] Vultr - Rẻ hơn
  - [ ] AWS EC2 - Free tier 12 tháng
  - [ ] Khác: _________________

### Thông Tin Cần Thu Thập

- [ ] Docker Hub Username: ____________________
- [ ] Email: ____________________
- [ ] Phone: ____________________

---

## 🖥️ Phase 2: Thuê và Setup VPS (Thời gian: 15-20 phút)

### Thuê VPS

- [ ] Đăng ký tài khoản VPS
- [ ] Verify payment method
- [ ] Tạo Droplet/Instance với config:
  - [ ] OS: Ubuntu 22.04 LTS
  - [ ] RAM: 2GB
  - [ ] CPU: 1 core
  - [ ] Storage: 50GB SSD
  - [ ] Region: Singapore
  - [ ] Price: ~$5-6/month

### Tạo SSH Key (QUAN TRỌNG)

#### Trên Windows (Git Bash):

```bash
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
```

- [ ] Nhấn Enter 3 lần
- [ ] Copy public key: `cat ~/.ssh/id_rsa.pub`
- [ ] Paste vào VPS provider khi tạo instance

### Lưu Thông Tin VPS

- [ ] Server IP: ____________________
- [ ] Username: ____________________
- [ ] SSH Port: ____________________ (mặc định: 22)
- [ ] SSH Key Path: ____________________

---

## 🔧 Phase 3: Cài Đặt Server (Thời gian: 10-15 phút)

### Kết Nối SSH

```bash
ssh root@YOUR_SERVER_IP
```

- [ ] Kết nối thành công
- [ ] Type "yes" nếu hỏi về fingerprint

### Option A: Auto Setup (Khuyến nghị)

```bash
curl -O https://raw.githubusercontent.com/cuong78/evRentalBE/main/scripts/setup-server.sh
chmod +x setup-server.sh
sudo ./setup-server.sh
```

- [ ] Script chạy thành công
- [ ] Docker installed
- [ ] Docker Compose installed
- [ ] Firewall configured
- [ ] Project directory created (`/opt/evrental`)
- [ ] `.env` file created
- [ ] SSH key created

**Copy SSH Private Key khi script hiển thị:**

- [ ] Copy toàn bộ từ `-----BEGIN` đến `-----END-----`
- [ ] Lưu vào notepad tạm

### Option B: Manual Setup

Nếu script không chạy, làm theo [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) Section 3.

---

## 🔑 Phase 4: GitHub Secrets (Thời gian: 5-10 phút)

### Docker Hub Token

- [ ] Đăng nhập https://hub.docker.com
- [ ] Account Settings → Security
- [ ] New Access Token
- [ ] Token name: "GitHub Actions"
- [ ] Permissions: Read, Write, Delete
- [ ] Generate token
- [ ] **COPY TOKEN NGAY** (chỉ hiện 1 lần!)

### Thêm Secrets vào GitHub

Repository → Settings → Secrets and variables → Actions → New repository secret

- [ ] `DOCKER_HUB_USERNAME` = ____________________
- [ ] `DOCKER_HUB_ACCESS_TOKEN` = (token vừa copy)
- [ ] `SERVER_HOST` = ____________________
- [ ] `SERVER_USER` = `root`
- [ ] `SERVER_SSH_KEY` = (private key từ Phase 3)
- [ ] `SERVER_PORT` = `22`

**Kiểm tra:**
- [ ] Tất cả 6 secrets đã được thêm
- [ ] Không có lỗi khi paste

---

## 📝 Phase 5: Cấu Hình Environment (Thời gian: 5 phút)

### Chỉnh sửa .env trên server

```bash
ssh root@YOUR_SERVER_IP
cd /opt/evrental
nano .env
```

**Cần thay đổi:**

- [ ] `POSTGRES_PASSWORD` - Mật khẩu database mạnh
- [ ] `JWT_SECRET` - JWT secret key (min 32 chars)
- [ ] `SPRING_MAIL_USERNAME` - Email của bạn
- [ ] `SPRING_MAIL_PASSWORD` - App password của Gmail
- [ ] `FRONTEND_URL_BASE` - URL frontend của bạn
- [ ] `PAYMENT_VNPAY_WALLET_RETURN_URL` - URL callback

**Lưu file:**
- [ ] Ctrl + X → Y → Enter

### Bảo mật file

```bash
chmod 600 .env
ls -la .env
```

- [ ] Quyền file: `-rw-------`

---

## 🚀 Phase 6: Deploy Lần Đầu (Thời gian: 15-20 phút)

### Cập nhật Code Local

- [ ] Mở file `docker-compose.yml`
- [ ] Kiểm tra `image: anhcuong8386/evrental-backend:latest`
- [ ] Commit changes:

```bash
git add .
git commit -m "Setup CI/CD for production"
git push origin main
```

### Theo Dõi Deployment

- [ ] Mở https://github.com/cuong78/evRentalBE
- [ ] Click tab **Actions**
- [ ] Xem workflow đang chạy
- [ ] Đợi tất cả steps thành công (màu xanh ✅)

**Thời gian ước tính:** 10-15 phút

### Kiểm Tra Logs

**GitHub Actions:**
- [ ] Build & Test ✅
- [ ] Docker Build & Push ✅
- [ ] Deploy to Server ✅
- [ ] Health Check ✅

**Server:**

```bash
ssh root@YOUR_SERVER_IP
cd /opt/evrental
docker compose ps
```

- [ ] `evrental-postgres` - Status: Up
- [ ] `evrental-app` - Status: Up

---

## ✅ Phase 7: Verification (Thời gian: 5 phút)

### Health Check

```bash
# Trên server
curl http://localhost:8080/actuator/health

# Hoặc từ browser
http://YOUR_SERVER_IP:8080/actuator/health
```

**Kết quả mong đợi:**
```json
{"status":"UP"}
```

- [ ] Health check trả về 200 OK
- [ ] Response: `{"status":"UP"}`

### Test API

```bash
curl http://YOUR_SERVER_IP:8080/api/v1/vehicles
```

- [ ] API trả về response (có thể empty array)
- [ ] Không có error 500

### Logs Check

```bash
cd /opt/evrental
docker compose logs --tail=50 app
```

**Kiểm tra:**
- [ ] Không có ERROR logs
- [ ] Thấy "Started EvRentalBeApplication"
- [ ] Database connected successfully

### Performance Check

```bash
./health-check.sh
```

- [ ] Container status: Up
- [ ] Memory usage: < 80%
- [ ] Disk space: > 20% free

---

## 🎯 Phase 8: Post-Deployment (Tùy chọn)

### Setup Domain (Nếu có)

- [ ] Mua domain
- [ ] Point A record đến Server IP
- [ ] Đợi DNS propagate (5-30 phút)
- [ ] Update `.env` với domain mới

### Setup HTTPS (Khuyến nghị)

- [ ] Cài đặt Nginx
- [ ] Cài đặt Certbot
- [ ] Generate SSL certificate
- [ ] Configure reverse proxy

**Xem:** [NGINX_HTTPS_SETUP.md](docs/NGINX_HTTPS_SETUP.md) (coming soon)

### Setup Monitoring

- [ ] Install monitoring tools (Prometheus, Grafana)
- [ ] Setup email alerts
- [ ] Configure backup scripts

### Database Backup

```bash
# Tạo backup script
nano /opt/evrental/backup.sh
```

- [ ] Schedule daily backup (cron)
- [ ] Test restore procedure

---

## 🔄 Workflow Sau Khi Setup

### Khi Có Code Mới:

1. [ ] Code trên local
2. [ ] Test local: `mvn test`
3. [ ] Commit: `git commit -m "message"`
4. [ ] Push: `git push origin main`
5. [ ] Đợi 10-15 phút
6. [ ] Kiểm tra GitHub Actions ✅
7. [ ] Verify API: `curl http://SERVER_IP:8080/actuator/health`

**XONG! Không cần SSH vào server!** 🎉

---

## 📞 Troubleshooting Checklist

### Nếu Container Không Start:

- [ ] Kiểm tra logs: `docker compose logs app`
- [ ] Kiểm tra `.env` file: `cat .env`
- [ ] Restart: `docker compose restart app`
- [ ] Rebuild: `docker compose down && docker compose up -d`

### Nếu Database Error:

- [ ] Check postgres logs: `docker compose logs postgres`
- [ ] Check connection: `docker compose exec app env | grep DATASOURCE`
- [ ] Verify password trong `.env`

### Nếu Port Không Truy Cập Được:

- [ ] Check firewall: `sudo ufw status`
- [ ] Open port: `sudo ufw allow 8080/tcp`
- [ ] Check listening: `netstat -tulpn | grep 8080`

### Nếu GitHub Actions Failed:

- [ ] Kiểm tra GitHub Secrets đã đúng chưa
- [ ] Test SSH: `ssh root@YOUR_SERVER_IP`
- [ ] Xem error logs trong Actions tab
- [ ] Re-run workflow

---

## 📊 Final Status Check

### Production Ready Checklist:

- [ ] ✅ Server đã setup và chạy
- [ ] ✅ Docker containers running
- [ ] ✅ Database connected
- [ ] ✅ Application health check pass
- [ ] ✅ GitHub Actions workflow success
- [ ] ✅ API endpoints accessible
- [ ] ✅ Logs không có errors
- [ ] ✅ `.env` file bảo mật
- [ ] ✅ SSH key configured
- [ ] ✅ Auto-deploy working

### Optional (Nâng cao):

- [ ] ⭐ Domain configured
- [ ] ⭐ HTTPS/SSL enabled
- [ ] ⭐ Monitoring setup
- [ ] ⭐ Backup automated
- [ ] ⭐ CDN configured
- [ ] ⭐ Load balancer (nếu cần)

---

## 🎉 Congratulations!

**Deployment thành công!** 

Bạn đã:
- ✅ Setup production server
- ✅ Configure CI/CD pipeline
- ✅ Deploy application successfully
- ✅ Verify everything works

**Next Steps:**
1. Develop more features
2. Push to main branch
3. Watch auto-deployment work! 🚀

---

## 📚 Tài Liệu Tham Khảo

- [QUICK_START_DEPLOY.md](QUICK_START_DEPLOY.md) - Quick start guide
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Chi tiết đầy đủ
- [README.md](README.md) - Project documentation

---

**Date Completed:** ____________________

**Server IP:** ____________________

**Notes:** 
_________________________________________
_________________________________________
_________________________________________
