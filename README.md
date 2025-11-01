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
- 📅 **Booking System** - Hệ thống đặt loại xe 
- 💰 **Payment Integration** - Tích hợp VNPay payment gateway
- 💳 **Wallet System** - Ví điện tử người dùng
- 📄 **Contract** - Tạo hợp đồng thuê xe 
- 🔄 **Return Transaction** - Hệ thống trả xe và hoàn tất giao dịch
- 🏪 **Rental Station** - Quản lý các trạm cho thuê
- 📧 **Email Notifications** - Gửi email thông báo
- ☁️ **Cloud Storage** - Lưu trữ hình ảnh với Cloudinary
- 🔍 **OCR Integration** - Nhận dạng văn bản từ hình ảnh với FPT.AI

## 🛠 Công Nghệ

- **Java 21** - Language
- **Spring Boot 3.5.5** - Framework
- **PostgreSQL 17.5** - Database
- **Docker & Docker Compose** - Containerization
- **Maven** - Build tool
- **JWT** - Authentication
- **VNPay** - Payment gateway
- **Cloudinary** - Image storage
- **FPT.AI OCR** - Optical Character Recognition
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



## 🔄 CI/CD

### GitHub Actions Workflow

Pipeline tự động khi push lên `main` branch:

1. **Build & Test** - Compile và chạy unit tests
2. **Docker Build** - Build Docker image
3. **Push to Docker Hub** - Push image lên registry
4. **Deploy to Server** - SSH và deploy lên production
5. **Health Check** - Kiểm tra application health


## 🔐 Security

- JWT token authentication
- Password hashing with BCrypt
- CORS configuration
- SQL injection protection (JPA/Hibernate)
- Environment variables for sensitive data
- HTTPS recommended for production



## 👥 Contributors

- **cuong78** - Backend Developer
- Group 4 Members

## 📧 Contact

- GitHub: [@cuong78](https://github.com/cuong78)
- Repository: [evRentalBE](https://github.com/cuong78/evRentalBE)

---

**Happy Coding! 🚀**
