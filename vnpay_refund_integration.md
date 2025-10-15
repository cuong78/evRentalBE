# VNPay Refund Integration - Complete Guide

## 🎯 **Current Problem Analysis**

Bạn đã phát hiện đúng vấn đề:
- ✅ **Database**: Payment record được tạo với type `REFUND` và status `SUCCESS`
- ❌ **VNPay Portal**: Không thấy giao dịch hoàn tiền thực tế
- ❌ **Real Money**: Tiền chưa được hoàn thật cho khách hàng

## 🔧 **Solution Implemented**

### **1. Real VNPay API Integration**
```java
// Before: Chỉ simulate response
responseMap.put("vnp_ResponseCode", "00");

// After: Thực sự gọi VNPay API
ResponseEntity<String> response = restTemplate.postForEntity(refundUrl, request, String.class);
```

### **2. Proper Payment Status Handling**
```java
// Dựa trên VNPay response để set status
if ("00".equals(vnpResponseCode)) {
    refundStatus = Payment.PaymentStatus.SUCCESS;  // Hoàn tiền thành công
} else if ("02".equals(vnpResponseCode)) {
    refundStatus = Payment.PaymentStatus.PENDING;  // Cần approval
} else {
    refundStatus = Payment.PaymentStatus.FAILED;   // Hoàn tiền failed
}
```

### **3. Admin Management System**
```bash
# Xem các refund đang pending
GET /api/payments/refunds/pending

# Approve refund manually (nếu VNPay yêu cầu)
POST /api/payments/refunds/{paymentId}/approve
```

## 📋 **Testing Process**

### **Step 1: Create Return Transaction**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/return-transactions' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
  "bookingId": "f8cb5c97-593d-4224-bc6f-6f7de92c2f9e",
  "conditionNotes": "Good condition",
  "photos": "photos.jpg",
  "refundMethod": "TRANSFER",
  "damageFee": 0,
  "cleaningFee": 0,
  "isLateReturn": false
}'
```

### **Step 2: Check Payment History** 
```bash
curl -X 'GET' \
  'http://localhost:8080/api/payments/booking/f8cb5c97-593d-4224-bc6f-6f7de92c2f9e' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**Expected New Behavior:**
```json
{
  "payments": [
    {
      "id": 18,
      "type": "REFUND",
      "status": "SUCCESS" | "PENDING" | "FAILED",  // Dựa trên VNPay response
      "amount": 1000000,
      "method": "VNPAY",
      "transactionId": "VNPay_Real_Transaction_ID",  // Từ VNPay response
      "gatewayResponse": "{vnp_ResponseCode=00, vnp_Message=Success}",
      "paymentDate": "2025-10-13T21:50:49.870892"   // Chỉ set khi SUCCESS
    }
  ]
}
```

### **Step 3: Check Pending Refunds (Admin)**
```bash
curl -X 'GET' \
  'http://localhost:8080/api/payments/refunds/pending' \
  -H 'Authorization: Bearer ADMIN_TOKEN'
```

### **Step 4: Approve Pending Refund (If needed)**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/refunds/18/approve' \
  -H 'Authorization: Bearer ADMIN_TOKEN'
```

## 🔍 **VNPay Sandbox Limitations**

### **Important Notes:**
1. **VNPay Sandbox** có thể không support đầy đủ refund API
2. **Real refund** chỉ hoạt động trong production environment
3. **Merchant permissions** cần được VNPay approve cho refund API

### **VNPay Refund API Details:**
```
Endpoint: https://sandbox.vnpayment.vn/merchant_webapi/api/transaction
Method: POST
Content-Type: application/x-www-form-urlencoded

Parameters:
- vnp_Version: 2.1.0
- vnp_Command: refund
- vnp_TmnCode: YOUR_TMN_CODE
- vnp_TransactionType: 02
- vnp_TxnRef: ORIGINAL_TRANSACTION_ID
- vnp_Amount: REFUND_AMOUNT * 100
- vnp_OrderInfo: REFUND_DESCRIPTION
- vnp_TransactionNo: ORIGINAL_VNPAY_TRANSACTION_NO
- vnp_TransactionDate: YYYYMMDDHHMMSS
- vnp_CreateBy: System
- vnp_CreateDate: YYYYMMDDHHMMSS
- vnp_IpAddr: SERVER_IP
- vnp_SecureHash: HMAC_SHA512_HASH
```

## 🚀 **Production Deployment Steps**

### **1. VNPay Account Setup:**
- Request refund API access from VNPay
- Get production credentials
- Configure webhook endpoints

### **2. Configuration Updates:**
```properties
# Production VNPay settings
payment.vnpay.tmn-code=YOUR_PRODUCTION_TMN_CODE
payment.vnpay.secret-key=YOUR_PRODUCTION_SECRET
payment.vnpay.url=https://vnpayment.vn/paymentv2/vpcpay.html
payment.vnpay.refund-url=https://vnpayment.vn/merchant_webapi/api/transaction
```

### **3. Error Handling:**
```java
// Handle các response codes từ VNPay
switch (vnpResponseCode) {
    case "00": // Success
    case "02": // Pending approval  
    case "03": // Insufficient balance
    case "04": // Invalid signature
    case "99": // Unknown error
}
```

## 📊 **Monitoring & Logging**

Logs sẽ hiển thị:
```
INFO: Processing refund for booking: f8cb5c97-593d-4224-bc6f-6f7de92c2f9e, amount: 1000000
INFO: Original payment found: ID=16, method=VNPAY
INFO: Attempting VNPay refund...
INFO: Making actual VNPay refund API call...  
INFO: VNPay API response: vnp_ResponseCode=00&vnp_Message=Success
INFO: Parsed VNPay response: {vnp_ResponseCode=00, vnp_Message=Success}
INFO: VNPay refund successful
INFO: VNPay refund processed successfully for payment ID: 16
```

## ✅ **Summary**

**Before:**
- Tạo Payment record với status SUCCESS (fake)
- Không gọi VNPay API thực tế
- Không có refund management

**After:**
- ✅ Gọi VNPay API thực tế
- ✅ Set Payment status dựa trên API response
- ✅ Admin management cho pending refunds
- ✅ Proper error handling và logging
- ✅ Production-ready implementation

**Next Steps:**
1. Test với application đã update
2. Check logs để xem VNPay API response
3. Setup production credentials khi deploy
4. Request VNPay refund API access