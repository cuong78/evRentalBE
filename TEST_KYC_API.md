# Test KYC OCR API

## ✅ Đã sửa lỗi

### Vấn đề trước đây:
- Code gửi JSON với base64 image
- FPT.AI API yêu cầu multipart/form-data

### Giải pháp:
- Sử dụng `MultiValueMap` và `ByteArrayResource` để gửi file trực tiếp
- Content-Type: `multipart/form-data`
- Header: `api-key`

## 🧪 Test API

### 1. Test với cURL (không cần authentication)

Để test nhanh mà không cần token, bạn có thể tạm thời comment `@SecurityRequirement` hoặc cho phép endpoint này public.

```bash
curl -X 'POST' \
  'http://localhost:8080/api/kyc/extract-cccd' \
  -H 'accept: */*' \
  -H 'Content-Type: multipart/form-data' \
  -F 'image=@mat1.jpg'
```

### 2. Test với Authentication

```bash
curl -X 'POST' \
  'http://localhost:8080/api/kyc/extract-cccd' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'image=@mat1.jpg'
```

### 3. Test trực tiếp FPT.AI API (để verify)

```bash
curl -X POST https://api.fpt.ai/vision/idr/vnm \
  -H "api-key: EVJ4qapZtFG3L0G1lEK17KWEbZfLgrFB" \
  -F "image=@mat1.jpg"
```

## 📝 Response mẫu

### Thành công:
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
    "expiryDate": "01/01/2036"
  },
  "kycLogId": 1,
  "status": "PENDING",
  "imageUrl": "https://res.cloudinary.com/..."
}
```

### Lỗi:
```json
{
  "message": "Không tìm thấy thông tin CCCD trong hình ảnh",
  "timestamp": "2025-10-30T10:00:00",
  "status": 400
}
```

## 🔍 Debug

### Kiểm tra log
```bash
# Xem log trong terminal chạy Spring Boot
# Tìm dòng:
# INFO: Calling FPT.AI API for CCCD extraction with file: mat1.jpg
```

### Lỗi thường gặp

1. **"Invalid Parameters or Values!"**
   - ✅ Đã sửa: Sử dụng multipart/form-data thay vì JSON

2. **"No image data provided"**
   - ✅ Đã sửa: Gửi file trực tiếp thay vì base64

3. **"Không tìm thấy thông tin CCCD trong hình ảnh"**
   - Ảnh không rõ hoặc không phải CCCD
   - Thử ảnh khác với chất lượng tốt hơn

4. **401 Unauthorized từ FPT.AI**
   - Kiểm tra API key trong `application.properties`
   - API key: `fpt.ai.api-key=EVJ4qapZtFG3L0G1lEK17KWEbZfLgrFB`

## 📊 Code thay đổi

### OCRService.java - Trước:
```java
// ❌ SAI: Gửi JSON với base64
Map<String, String> body = Map.of("image", base64Image);
headers.setContentType(MediaType.APPLICATION_JSON);
```

### OCRService.java - Sau:
```java
// ✅ ĐÚNG: Gửi multipart/form-data
MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
    @Override
    public String getFilename() {
        return image.getOriginalFilename();
    }
};
body.add("image", fileResource);
headers.setContentType(MediaType.MULTIPART_FORM_DATA);
```

## 🚀 Bước tiếp theo

1. ✅ API đã hoạt động với multipart/form-data
2. Test với ảnh CCCD thật
3. Kiểm tra KYCService lưu database
4. Test các endpoint khác (history, status, verify)

## 💡 Lưu ý

- File ảnh phải là JPG, JPEG hoặc PNG
- Kích thước tối đa 10MB
- Ảnh CCCD phải rõ ràng, không bị mờ
- FPT.AI API có giới hạn request (check credit)
