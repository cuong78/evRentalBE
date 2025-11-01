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

Drop tables if exists (in reverse order of dependencies)
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS kyc_logs CASCADE;
DROP TABLE IF EXISTS verification_token CASCADE;
DROP TABLE IF EXISTS password_reset_token CASCADE;
DROP TABLE IF EXISTS refresh_token CASCADE;
DROP TABLE IF EXISTS topup_bill CASCADE;
DROP TABLE IF EXISTS return_transaction CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS contract CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS document CASCADE;
DROP TABLE IF EXISTS wallet CASCADE;
DROP TABLE IF EXISTS vehicle CASCADE;
DROP TABLE IF EXISTS vehicle_type CASCADE;
DROP TABLE IF EXISTS rental_station CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- =====================================================
-- CORE TABLES
-- =====================================================

-- Permissions Table
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles Table
CREATE TABLE roles (
    name VARCHAR(255) PRIMARY KEY,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users Table
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_verify BOOLEAN NOT NULL DEFAULT FALSE,
    token_version INTEGER DEFAULT 0,
    managed_station_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rental Station Table
CREATE TABLE rental_station (
    id BIGSERIAL PRIMARY KEY,
    city VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    admin_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Add foreign key for managed_station_id after rental_station is created
ALTER TABLE users
ADD CONSTRAINT fk_users_managed_station 
FOREIGN KEY (managed_station_id) REFERENCES rental_station(id) ON DELETE SET NULL;

-- Wallet Table
CREATE TABLE wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Document Table
CREATE TABLE document (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL CHECK (document_type IN ('CMND', 'CCCD', 'PASSPORT', 'DRIVING_LICENSE')),
    document_number VARCHAR(255) NOT NULL,
    front_photo TEXT,
    back_photo TEXT,
    issue_date DATE,
    expiry_date DATE,
    issued_by VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'REJECTED')),
    verified_at TIMESTAMP,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Vehicle Type Table
CREATE TABLE vehicle_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    deposit_amount DOUBLE PRECISION NOT NULL,
    rental_rate DOUBLE PRECISION NOT NULL,
    image_url VARCHAR(500),
    seats INTEGER NOT NULL,
    range INTEGER NOT NULL,
    range_standard VARCHAR(50),
    trunk_capacity INTEGER NOT NULL,
    category VARCHAR(50),
    description VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vehicle Table
CREATE TABLE vehicle (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RENTED', 'DAMAGED', 'MAINTENANCE')),
    license_plate VARCHAR(20) UNIQUE,
    condition_notes TEXT,
    photos TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id) REFERENCES vehicle_type(id) ON DELETE RESTRICT,
    FOREIGN KEY (station_id) REFERENCES rental_station(id) ON DELETE RESTRICT
);

-- Booking Table
CREATE TABLE booking (
    id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('VNPAY', 'WALLET')),
    total_payment DOUBLE PRECISION NOT NULL,
    is_paid_by_wallet BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    payment_expiry_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (station_id) REFERENCES rental_station(id) ON DELETE RESTRICT,
    FOREIGN KEY (type_id) REFERENCES vehicle_type(id) ON DELETE RESTRICT
);

-- Payment Table
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    booking_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('DEPOSIT', 'REFUND')),
    method VARCHAR(50) NOT NULL CHECK (method IN ('VNPAY', 'WALLET')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED')),
    amount DOUBLE PRECISION NOT NULL,
    transaction_id VARCHAR(255),
    description TEXT,
    gateway_response TEXT,
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE
);

-- Contract Table
CREATE TABLE contract (
    id BIGSERIAL PRIMARY KEY,
    booking_id VARCHAR(255) NOT NULL UNIQUE,
    vehicle_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    condition_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE RESTRICT,
    FOREIGN KEY (document_id) REFERENCES document(id) ON DELETE RESTRICT
);

-- Return Transaction Table
CREATE TABLE return_transaction (
    id BIGSERIAL PRIMARY KEY,
    booking_id VARCHAR(255) NOT NULL UNIQUE,
    return_date TIMESTAMP NOT NULL,
    additional_fees DOUBLE PRECISION DEFAULT 0,
    refund_amount DOUBLE PRECISION NOT NULL DEFAULT 0,
    condition_notes TEXT,
    photos TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE
);

-- Topup Bill Table
CREATE TABLE topup_bill (
    id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'EXPIRED')),
    transaction_id VARCHAR(255),
    gateway_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- KYC Logs Table
CREATE TABLE kyc_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    cccd_id VARCHAR(20),
    full_name VARCHAR(255),
    date_of_birth VARCHAR(20),
    gender VARCHAR(10),
    nationality VARCHAR(50),
    place_of_origin VARCHAR(255),
    place_of_residence VARCHAR(255),
    issue_date VARCHAR(20),
    issued_by VARCHAR(255),
    expiry_date VARCHAR(20),
    front_image_url VARCHAR(500),
    back_image_url VARCHAR(500),
    verification_status VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =====================================================
-- AUTHENTICATION & SECURITY TABLES
-- =====================================================

-- Verification Token Table
CREATE TABLE verification_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255),
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Password Reset Token Table
CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255),
    user_id BIGINT,
    expiry_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Refresh Token Table
CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =====================================================
-- JUNCTION TABLES (Many-to-Many)
-- =====================================================

-- User-Roles Junction Table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_name) REFERENCES roles(name) ON DELETE CASCADE
);

-- Role-Permissions Junction Table
CREATE TABLE role_permissions (
    role_name VARCHAR(255) NOT NULL,
    permission_name BIGINT NOT NULL,
    PRIMARY KEY (role_name, permission_name),
    FOREIGN KEY (role_name) REFERENCES roles(name) ON DELETE CASCADE,
    FOREIGN KEY (permission_name) REFERENCES permissions(id) ON DELETE CASCADE
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_station ON users(managed_station_id);

-- Booking indexes
CREATE INDEX idx_booking_user ON booking(user_id);
CREATE INDEX idx_booking_station ON booking(station_id);
CREATE INDEX idx_booking_type ON booking(type_id);
CREATE INDEX idx_booking_status ON booking(status);
CREATE INDEX idx_booking_dates ON booking(start_date, end_date);

-- Vehicle indexes
CREATE INDEX idx_vehicle_type ON vehicle(type_id);
CREATE INDEX idx_vehicle_station ON vehicle(station_id);
CREATE INDEX idx_vehicle_status ON vehicle(status);

-- Payment indexes
CREATE INDEX idx_payment_booking ON payment(booking_id);
CREATE INDEX idx_payment_status ON payment(status);
CREATE INDEX idx_payment_transaction ON payment(transaction_id);

-- Document indexes
CREATE INDEX idx_document_user ON document(user_id);
CREATE INDEX idx_document_status ON document(status);

-- Contract indexes
CREATE INDEX idx_contract_booking ON contract(booking_id);
CREATE INDEX idx_contract_vehicle ON contract(vehicle_id);

-- KYC indexes
CREATE INDEX idx_kyc_user ON kyc_logs(user_id);
CREATE INDEX idx_kyc_status ON kyc_logs(verification_status);

-- =====================================================
-- INITIAL DATA
-- =====================================================

-- Insert default permissions
INSERT INTO permissions (name, code, description) VALUES
('Create User', 'CREATE_USER', 'Permission to create new users'),
('Update User', 'UPDATE_USER', 'Permission to update user information'),
('Delete User', 'DELETE_USER', 'Permission to delete users'),
('View User', 'VIEW_USER', 'Permission to view user information'),
('Manage Roles', 'MANAGE_ROLES', 'Permission to manage user roles'),
('Create Vehicle', 'CREATE_VEHICLE', 'Permission to create new vehicles'),
('Update Vehicle', 'UPDATE_VEHICLE', 'Permission to update vehicle information'),
('Delete Vehicle', 'DELETE_VEHICLE', 'Permission to delete vehicles'),
('View Vehicle', 'VIEW_VEHICLE', 'Permission to view vehicle information'),
('Create Booking', 'CREATE_BOOKING', 'Permission to create bookings'),
('Update Booking', 'UPDATE_BOOKING', 'Permission to update bookings'),
('Cancel Booking', 'CANCEL_BOOKING', 'Permission to cancel bookings'),
('View Booking', 'VIEW_BOOKING', 'Permission to view bookings'),
('Manage Station', 'MANAGE_STATION', 'Permission to manage rental stations'),
('Process Payment', 'PROCESS_PAYMENT', 'Permission to process payments'),
('Verify KYC', 'VERIFY_KYC', 'Permission to verify KYC documents'),
('Manage Contracts', 'MANAGE_CONTRACTS', 'Permission to manage rental contracts'),
('Process Returns', 'PROCESS_RETURNS', 'Permission to process vehicle returns');

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'System Administrator with full access'),
('STAFF', 'Station staff member'),
('USER', 'Regular customer user');

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON TABLE roles IS 'Defines user roles in the system';
COMMENT ON TABLE permissions IS 'Defines granular permissions';
COMMENT ON TABLE rental_station IS 'Physical rental station locations';
COMMENT ON TABLE vehicle_type IS 'Types/models of electric vehicles';
COMMENT ON TABLE vehicle IS 'Individual vehicle instances';
COMMENT ON TABLE booking IS 'Vehicle rental bookings';
COMMENT ON TABLE contract IS 'Rental contracts linking bookings to vehicles';
COMMENT ON TABLE payment IS 'Payment transactions';
COMMENT ON TABLE return_transaction IS 'Vehicle return records';
COMMENT ON TABLE wallet IS 'User digital wallet balances';
COMMENT ON TABLE document IS 'User identity documents (CCCD, licenses)';
COMMENT ON TABLE kyc_logs IS 'KYC verification logs using OCR';
COMMENT ON TABLE topup_bill IS 'Wallet top-up transactions';





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
