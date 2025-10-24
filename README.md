# 🚗 EVRental Backend

Backend service cho hệ thống cho thuê xe điện (Electric Vehicle Rental).

## 📋 Mục Lục

- [Tính năng](#-tính-năng)
- [Công nghệ](#-công-nghệ)
- [Cài đặt Local](#-cài-đặt-local)
- [Deploy Production](#-deploy-production)
- [API Documentation](#-api-documentation)
- [CI/CD](#-cicd)

## ✨ Tính Năng

- 🔐 **Authentication & Authorization** - JWT-based authentication
- 👤 **User Management** - Quản lý người dùng và phân quyền
- 🚗 **Vehicle Management** - Quản lý xe điện cho thuê
- 📅 **Booking System** - Hệ thống đặt xe
- 💰 **Payment Integration** - Tích hợp VNPay payment gateway
- 💳 **Wallet System** - Ví điện tử người dùng
- 📄 **Contract Management** - Quản lý hợp đồng thuê xe
- 🏪 **Rental Station** - Quản lý các trạm cho thuê
- 📧 **Email Notifications** - Gửi email thông báo
- ☁️ **Cloud Storage** - Lưu trữ hình ảnh với Cloudinary

## 🛠 Công Nghệ

- **Java 21** - Language
- **Spring Boot 3.5.5** - Framework
- **PostgreSQL 17.5** - Database
- **Docker & Docker Compose** - Containerization
- **Maven** - Build tool
- **JWT** - Authentication
- **VNPay** - Payment gateway
- **Cloudinary** - Image storage
- **GitHub Actions** - CI/CD

## 🚀 Cài Đặt Local

### Prerequisites

- Java 21+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 17+ (hoặc dùng Docker)

### Clone Repository

```bash
git clone https://github.com/cuong78/evRentalBE.git
cd evRentalBE
```

### Setup Database

#### Option 1: Sử dụng Docker Compose (Khuyến nghị)

```bash
# Chỉ chạy PostgreSQL
docker compose up -d postgres

# Kiểm tra
docker compose ps
```

#### Option 2: PostgreSQL local

```sql
CREATE DATABASE evrental;
CREATE USER postgres WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE evrental TO postgres;
```

### Cấu Hình

Cập nhật `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/evrental
spring.datasource.username=postgres
spring.datasource.password=123456

# JWT (thay đổi cho production)
jwt.secret=your-secret-key-here
jwt.expiration.ms=3600000

# Email (Gmail)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# VNPay (sandbox)
payment.vnpay.tmn-code=F9EZ1X6Z
payment.vnpay.secret-key=XV3C5IN6HACS0NWN5OHG1IDLYOZWI1VX

# Cloudinary
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret
```

### Chạy Application

#### Option 1: Maven

```bash
# Compile
mvn clean compile

# Run
mvn spring-boot:run

# Hoặc build và run
mvn clean package -DskipTests
java -jar target/*.jar
```

#### Option 2: Docker Compose (Full stack)

```bash
# Build và chạy
docker compose up -d

# Xem logs
docker compose logs -f app

# Dừng
docker compose down
```

### Kiểm Tra

```bash
# Health check
curl http://localhost:8080/actuator/health

# Response: {"status":"UP"}
```

## 🌐 Deploy Production

### Quick Deploy (10-15 phút)

Xem hướng dẫn chi tiết: **[QUICK_START_DEPLOY.md](QUICK_START_DEPLOY.md)**

### Các bước tóm tắt:

1. **Thuê VPS** - DigitalOcean/Vultr ($5-6/tháng)
2. **Setup Server** - Chạy script tự động
3. **Configure GitHub Secrets** - Docker Hub + SSH
4. **Push Code** - Tự động deploy!

```bash
# Trên server
curl -O https://raw.githubusercontent.com/cuong78/evRentalBE/main/scripts/setup-server.sh
chmod +x setup-server.sh
sudo ./setup-server.sh

# Trên local
git push origin main
```

**Chi tiết đầy đủ:** [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

## 📚 API Documentation

### Base URL

```
Development: http://localhost:8080
Production: http://your-server-ip:8080
```

### Authentication

```bash
# Register
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "0123456789"
}

# Login
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "expiresIn": 3600000
}
```

### Vehicles

```bash
# Get all vehicles
GET /api/v1/vehicles

# Get vehicle by ID
GET /api/v1/vehicles/{id}

# Create vehicle (Admin only)
POST /api/v1/vehicles
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Tesla Model 3",
  "vehicleTypeId": 1,
  "licensePlate": "29A-12345",
  "pricePerDay": 500000,
  "status": "AVAILABLE"
}
```

### Bookings

```bash
# Create booking
POST /api/v1/bookings
Authorization: Bearer {token}

{
  "vehicleId": 1,
  "startDate": "2025-01-01",
  "endDate": "2025-01-05",
  "rentalStationId": 1
}

# Get user bookings
GET /api/v1/bookings/user
Authorization: Bearer {token}
```

### Payment

```bash
# Create VNPay payment
POST /api/v1/payment/create-payment
Authorization: Bearer {token}

{
  "amount": 2000000,
  "orderInfo": "Thanh toán đặt xe",
  "bookingId": 123
}

# Wallet top-up
POST /api/v1/wallet/topup
Authorization: Bearer {token}

{
  "amount": 1000000
}
```

**Full API Documentation:** Sử dụng Swagger UI tại `/swagger-ui.html` (khi enable)

## 🔄 CI/CD

### GitHub Actions Workflow

Pipeline tự động khi push lên `main` branch:

1. **Build & Test** - Compile và chạy unit tests
2. **Docker Build** - Build Docker image
3. **Push to Docker Hub** - Push image lên registry
4. **Deploy to Server** - SSH và deploy lên production
5. **Health Check** - Kiểm tra application health

### Monitoring Deployment

```bash
# Xem workflow
https://github.com/cuong78/evRentalBE/actions

# SSH vào server
ssh root@your-server-ip
cd /opt/evrental

# Xem logs
docker compose logs -f app

# Health check
./health-check.sh
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest

# Generate test report
mvn test jacoco:report
```

## 📊 Database Schema

```sql
-- Main tables
Users
Vehicles
VehicleTypes
RentalStations
Bookings
Contracts
Payments
Wallets
WalletTransactions
Documents
ReturnTransactions
```

**Xem chi tiết:** Chạy app và kiểm tra database sau khi Hibernate tạo tables

## 🔐 Security

- JWT token authentication
- Password hashing with BCrypt
- CORS configuration
- SQL injection protection (JPA/Hibernate)
- Environment variables for sensitive data
- HTTPS recommended for production

## 🐛 Troubleshooting

### Application không start

```bash
# Kiểm tra logs
docker compose logs app

# Kiểm tra database
docker compose logs postgres

# Restart
docker compose restart app
```

### Database connection error

```bash
# Kiểm tra PostgreSQL
docker compose ps postgres

# Test connection
docker compose exec postgres psql -U postgres -d evrental
```

### Port đã được sử dụng

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux
lsof -i :8080
kill -9 <PID>
```

## 📝 License

Dự án thuộc về Group 4 - HCMUT

## 👥 Contributors

- **cuong78** - Backend Developer
- Group 4 Members

## 📧 Contact

- GitHub: [@cuong78](https://github.com/cuong78)
- Repository: [evRentalBE](https://github.com/cuong78/evRentalBE)

---

**Happy Coding! 🚀**
