# KYC OCR Feature - Hướng dẫn sử dụng

## Tổng quan
Tính năng KYC (Know Your Customer) OCR cho phép trích xuất thông tin từ ảnh CCCD (Căn cước công dân) sử dụng FPT.AI API.

## Các tính năng chính

### 1. Trích xuất thông tin CCCD
- **Endpoint**: `POST /api/kyc/extract-cccd`
- **Authentication**: Bearer Token required
- **Content-Type**: multipart/form-data

**Thông tin trích xuất được:**
- Số CCCD
- Họ và tên
- Ngày sinh
- Giới tính
- Quốc tịch
- Quê quán
- Nơi thường trú
- Ngày hết hạn

### 2. Lịch sử KYC
- **Endpoint**: `GET /api/kyc/history`
- Xem lịch sử các lần xác thực KYC

### 3. Kiểm tra trạng thái KYC
- **Endpoint**: `GET /api/kyc/status`
- Kiểm tra trạng thái xác thực hiện tại

### 4. Xác thực KYC (Admin)
- **Endpoint**: `PUT /api/kyc/verify/{kycLogId}`
- Admin duyệt hoặc từ chối KYC

### 5. Danh sách KYC chờ duyệt (Admin)
- **Endpoint**: `GET /api/kyc/pending`
- Lấy danh sách KYC chờ xác thực

## Cách sử dụng

### 1. Trích xuất thông tin CCCD

```bash
curl -X 'POST' \
  'http://localhost:8080/api/kyc/extract-cccd' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'image=@/path/to/cccd_image.jpg;type=image/jpeg'
```

**Response thành công:**
```json
{
  "cccdInfo": {
    "id": "001234567890",
    "fullName": "NGUYEN VAN A",
    "dateOfBirth": "01/01/1990",
    "gender": "Nam",
    "nationality": "Việt Nam",
    "placeOfOrigin": "Hà Nội",
    "placeOfResidence": "123 Đường ABC, Quận XYZ, TP. Hà Nội",
    "issueDate": "01/01/2021",
    "expiryDate": "01/01/2036"
  },
  "kycLogId": 1,
  "status": "PENDING",
  "imageUrl": "https://cloudinary.com/..."
}
```

### 2. Kiểm tra lịch sử KYC

```bash
curl -X 'GET' \
  'http://localhost:8080/api/kyc/history' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### 3. Kiểm tra trạng thái KYC

```bash
curl -X 'GET' \
  'http://localhost:8080/api/kyc/status' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### 4. Admin xác thực KYC

```bash
curl -X 'PUT' \
  'http://localhost:8080/api/kyc/verify/1?status=APPROVED&notes=Đã%20xác%20thực' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer ADMIN_JWT_TOKEN'
```

**Trạng thái KYC:**
- `PENDING`: Chờ xác thực
- `APPROVED`: Đã duyệt
- `REJECTED`: Từ chối

## Yêu cầu và giới hạn

### Upload file
- **Kích thước tối đa**: 10MB
- **Định dạng hỗ trợ**: JPG, JPEG, PNG
- **Content-Type**: image/jpeg, image/jpg, image/png

### FPT.AI API
- Cần cấu hình API key trong `application.properties`:
  ```properties
  fpt.ai.api-key=YOUR_API_KEY
  ```
- API endpoint: https://api.fpt.ai/vision/idr/vnm

## Cấu trúc Database

### Bảng `kyc_logs`
```sql
CREATE TABLE kyc_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    cccd_id VARCHAR(20),
    full_name VARCHAR(255),
    date_of_birth VARCHAR(20),
    gender VARCHAR(10),
    nationality VARCHAR(50),
    place_of_origin VARCHAR(255),
    place_of_residence VARCHAR(255),
    issue_date VARCHAR(20),
    expiry_date VARCHAR(20),
    image_url VARCHAR(500),
    verification_status VARCHAR(20),
    created_at TIMESTAMP,
    verified_at TIMESTAMP,
    notes TEXT
);
```

## Xử lý lỗi

### Lỗi phổ biến

1. **"Vui lòng upload ảnh CCCD"**
   - File không được upload hoặc trống

2. **"Kích thước file không được vượt quá 10MB"**
   - File quá lớn

3. **"Chỉ chấp nhận file ảnh định dạng JPG, JPEG hoặc PNG"**
   - Định dạng file không hợp lệ

4. **"Không tìm thấy thông tin CCCD trong hình ảnh"**
   - Ảnh không chứa thông tin CCCD hoặc chất lượng kém

5. **"Lỗi từ FPT.AI: ..."**
   - Lỗi từ API FPT.AI (kiểm tra API key, credit, v.v.)

## Testing

### Postman Collection
Import file `KYC_API.postman_collection.json` để test các endpoint.

### Sample Images
Sử dụng ảnh CCCD mẫu trong thư mục `test-images/` để test.

## Security

1. **Authentication**: Tất cả endpoint yêu cầu JWT token
2. **Authorization**: 
   - User endpoints: Chỉ truy cập dữ liệu của chính mình
   - Admin endpoints: Yêu cầu role ADMIN
3. **File validation**: Kiểm tra kỹ file trước khi xử lý
4. **Data encryption**: Thông tin nhạy cảm được mã hóa khi lưu trữ

## Monitoring & Logging

- Tất cả request được log với level INFO
- Lỗi được log với level ERROR
- Sử dụng `@Slf4j` annotation của Lombok

## Future Enhancements

1. Hỗ trợ trích xuất thông tin từ nhiều loại giấy tờ khác
2. Tích hợp AI để phát hiện CCCD giả mạo
3. Tự động so sánh ảnh selfie với ảnh trên CCCD
4. Webhook notification khi KYC được duyệt/từ chối
5. Export báo cáo KYC theo thời gian
