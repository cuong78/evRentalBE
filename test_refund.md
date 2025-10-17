# Test Refund Process

## Hiện tại đã sửa các vấn đề sau:

### 1. ✅ Fixed ReturnTransactionResponse missing fields
- `isLateReturn`: Giờ được tính toán đúng từ returnDate vs endDate
- `overdueDays`: Tính số ngày trễ chính xác  
- `originalDeposit`: Lấy từ VehicleType.depositAmount
- `refundStatus`: Hiển thị trạng thái hoàn tiền thực tế

### 2. ✅ Enhanced VNPay refund process
- Thêm comprehensive logging cho debug
- Better error handling - không làm fail transaction
- VNPay API integration (simulation cho sandbox)

### 3. ✅ Added Payment debugging endpoint
- `GET /api/payments/booking/{bookingId}` - Xem toàn bộ payment history

## Test Steps:

### Step 1: Kiểm tra Payment History (TRƯỚC khi return)
```bash
curl -X 'GET' \
  'http://localhost:8080/api/payments/booking/efb29a99-2cf4-4e32-8e7a-5b0ad426a644' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

### Step 2: Create Return Transaction
```bash
curl -X 'POST' \
  'http://localhost:8080/api/return-transactions' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
  "bookingId": "efb29a99-2cf4-4e32-8e7a-5b0ad426a644",
  "conditionNotes": "Vehicle returned in good condition",
  "photos": "return_photos.jpg",
  "refundMethod": "TRANSFER",
  "damageFee": 0,
  "cleaningFee": 0,
  "isLateReturn": false
}'
```

### Step 3: Kiểm tra Payment History (SAU khi return)
```bash
curl -X 'GET' \
  'http://localhost:8080/api/payments/booking/efb29a99-2cf4-4e32-8e7a-5b0ad426a644' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**Expected Result:**
- Sẽ có thêm 1 payment record với type `REFUND`
- ReturnTransactionResponse sẽ có đầy đủ các fields
- Application logs sẽ show VNPay refund process

## VNPay Sandbox Limitation:

**Important:** VNPay sandbox có thể không hỗ trợ đầy đủ refund API trong môi trường test. 
Điều này là bình thường cho sandbox environment.

Trong production, bạn cần:
1. Sử dụng VNPay production credentials
2. Call actual VNPay refund API endpoint
3. Handle real API responses

Hiện tại system đã:
- ✅ Tạo Payment record với type REFUND
- ✅ Log VNPay API call (simulated) 
- ✅ Update refund status correctly
- ✅ Show complete transaction history

## Debugging:

Nếu vẫn có vấn đề, check:
1. Application logs trong terminal
2. Database Payment table có record REFUND không
3. ReturnTransaction response có đầy đủ fields không