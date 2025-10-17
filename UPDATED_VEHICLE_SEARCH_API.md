# 🎯 Updated Vehicle Search API - Search by Station

## ✅ Đã sửa Business Logic

### ❌ **Trước (SAI):**
```json
{
  "stationId": 1,
  "typeId": 2,  // ← SAI: Search từng loại xe riêng lẻ
  "startDate": "2025-02-01T10:00:00",
  "endDate": "2025-02-05T10:00:00"
}
```
**Vấn đề:** Phải gọi API nhiều lần cho mỗi loại xe → Vô lý!

### ✅ **Sau (ĐÚNG):**
```json
{
  "stationId": 1,  // Chỉ cần station + dates
  "startDate": "2025-02-01T10:00:00",
  "endDate": "2025-02-05T10:00:00"
}
```
**Kết quả:** Trả về **TẤT CẢ loại xe available** tại station đó!

---

## 📝 API Endpoint

### `POST /api/vehicles/search` hoặc `GET /api/vehicles/search`

**Request:**
```json
{
  "stationId": 1,
  "startDate": "2025-02-01T10:00:00",
  "endDate": "2025-02-05T10:00:00"
}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Vehicle availability retrieved successfully",
  "data": {
    "stationId": 1,
    "stationName": "Hanoi - 123 Nguyen Trai Street",
    "searchStartDate": "2025-02-01T10:00:00",
    "searchEndDate": "2025-02-05T10:00:00",
    "vehicleTypes": [
      {
        "typeId": 1,
        "typeName": "Honda Vision",
        "depositAmount": 2000000.0,
        "rentalRate": 150000.0,
        "totalVehicles": 10,
        "availableCount": 7,
        "availableVehicles": [
          {
            "id": 1,
            "typeId": 1,
            "stationId": 1,
            "status": "AVAILABLE",
            "conditionNotes": "Good condition",
            "photos": "vehicle1.jpg"
          },
          // ... 6 more vehicles
        ]
      },
      {
        "typeId": 2,
        "typeName": "Yamaha Grande",
        "depositAmount": 2500000.0,
        "rentalRate": 180000.0,
        "totalVehicles": 8,
        "availableCount": 5,
        "availableVehicles": [...]
      },
      {
        "typeId": 3,
        "typeName": "SH Mode",
        "depositAmount": 3000000.0,
        "rentalRate": 250000.0,
        "totalVehicles": 5,
        "availableCount": 3,
        "availableVehicles": [...]
      }
    ]
  }
}
```

---

## 🔍 Response Structure

### Top Level
- `stationId` - ID của station được search
- `stationName` - Tên và địa chỉ station
- `searchStartDate` - Ngày bắt đầu thuê
- `searchEndDate` - Ngày trả xe
- `vehicleTypes` - **Danh sách TẤT CẢ loại xe available**

### VehicleTypeAvailability (cho mỗi loại xe)
- `typeId` - ID loại xe
- `typeName` - Tên loại xe (Honda Vision, Yamaha Grande, etc.)
- `depositAmount` - Tiền đặt cọc
- `rentalRate` - Giá thuê/ngày
- `totalVehicles` - **Tổng số xe loại này tại station**
- `availableCount` - **Số xe available trong khoảng thời gian**
- `availableVehicles` - **Danh sách chi tiết các xe available**

---

## 💡 Use Cases

### Use Case 1: Customer tìm xe tại Hanoi
```http
POST /api/vehicles/search
{
  "stationId": 1,
  "startDate": "2025-02-10T09:00:00",
  "endDate": "2025-02-15T18:00:00"
}
```

**Response sẽ hiển thị:**
- Honda Vision: 7/10 xe available
- Yamaha Grande: 5/8 xe available
- SH Mode: 3/5 xe available
- Honda Air Blade: 2/6 xe available

→ Customer chọn loại xe phù hợp với budget!

### Use Case 2: Admin kiểm tra inventory
```http
POST /api/vehicles/search
{
  "stationId": 2,
  "startDate": "2025-03-01T00:00:00",
  "endDate": "2025-03-31T23:59:59"
}
```

**Response:** Overview toàn bộ xe available trong tháng 3

---

## 🚀 Backend Changes

### 1. VehicleSearchRequest
```java
// ❌ Removed
private Long typeId;

// ✅ Kept
private Long stationId;
private LocalDateTime startDate;
private LocalDateTime endDate;
```

### 2. VehicleAvailabilityResponse
```java
// ❌ Old: Single type response
private Long typeId;
private String typeName;
private List<VehicleResponse> availableVehicles;

// ✅ New: Multiple types response
private Long stationId;
private String stationName;
private List<VehicleTypeAvailability> vehicleTypes;

// Inner class cho mỗi loại xe
public static class VehicleTypeAvailability {
    private Long typeId;
    private String typeName;
    private Double depositAmount;
    private Double rentalRate;
    private Integer totalVehicles;
    private Integer availableCount;
    private List<VehicleResponse> availableVehicles;
}
```

### 3. VehicleRepository
```java
// ✅ New method: Lấy TẤT CẢ xe available (không filter type)
@Query(value = """
    SELECT DISTINCT v.* 
    FROM vehicle v
    WHERE v.station_id = :stationId
      AND v.status = 'AVAILABLE'
      AND v.id NOT IN (
          -- Conflict detection logic...
      )
    ORDER BY v.type_id, v.id
    """, nativeQuery = true)
List<Vehicle> findAvailableVehiclesByStation(
    @Param("stationId") Long stationId,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);
```

### 4. VehicleServiceImpl
```java
// Query tất cả xe available
List<Vehicle> allAvailableVehicles = 
    vehicleRepository.findAvailableVehiclesByStation(
        searchRequest.getStationId(),
        searchRequest.getStartDate(),
        searchRequest.getEndDate()
    );

// Group by vehicle type
Map<VehicleType, List<Vehicle>> vehiclesByType = 
    allAvailableVehicles.stream()
        .collect(Collectors.groupingBy(Vehicle::getType));

// Build response for each type
List<VehicleTypeAvailability> vehicleTypeAvailabilities = 
    vehiclesByType.entrySet().stream()
        .map(entry -> {
            // Count total, build response for each type
        })
        .sorted((a, b) -> a.getTypeId().compareTo(b.getTypeId()))
        .collect(Collectors.toList());
```

---

## 📊 Performance

### Queries
- **1 query** để lấy tất cả xe available
- **N queries** để count total vehicles (N = số loại xe)
- Total: **1 + N queries** (N thường < 10)

### Example với 5 loại xe:
- Old: User phải gọi API **5 lần** (mỗi loại 1 lần)
- New: User gọi API **1 lần**, backend chạy **6 queries**

**Trade-off:** Backend phức tạp hơn, nhưng UX tốt hơn rất nhiều!

---

## 🎨 Frontend Display

```javascript
// Response data
const response = {
  stationName: "Hanoi - 123 Nguyen Trai",
  vehicleTypes: [...]
};

// Display như này:
┌─────────────────────────────────────┐
│  Hanoi - 123 Nguyen Trai           │
│  01/02/2025 → 05/02/2025           │
├─────────────────────────────────────┤
│  Honda Vision          150k/day    │
│  💰 Deposit: 2M                    │
│  📊 Available: 7/10                │
│  [View Details] [Book Now]         │
├─────────────────────────────────────┤
│  Yamaha Grande         180k/day    │
│  💰 Deposit: 2.5M                  │
│  📊 Available: 5/8                 │
│  [View Details] [Book Now]         │
├─────────────────────────────────────┤
│  SH Mode               250k/day    │
│  💰 Deposit: 3M                    │
│  📊 Available: 3/5                 │
│  [View Details] [Book Now]         │
└─────────────────────────────────────┘
```

---

## ✅ Benefits

1. **UX tốt hơn:** User thấy tất cả options trong 1 màn hình
2. **Giảm API calls:** 5 calls → 1 call
3. **Easy comparison:** So sánh giá và availability dễ dàng
4. **Business insight:** Thấy overview toàn bộ inventory
5. **Logical flow:** Search station → See all types → Pick one

---

## 🧪 Test Cases

### Test 1: Station có nhiều loại xe
**Input:**
```json
{
  "stationId": 1,
  "startDate": "2025-02-01T10:00:00",
  "endDate": "2025-02-05T10:00:00"
}
```

**Expected:** Response có 3-5 loại xe với availability khác nhau

### Test 2: Station chỉ có 1 loại xe
**Input:**
```json
{
  "stationId": 3,
  "startDate": "2025-02-01T10:00:00",
  "endDate": "2025-02-05T10:00:00"
}
```

**Expected:** Response có 1 vehicleType duy nhất

### Test 3: Tất cả xe đều bận
**Input:**
```json
{
  "stationId": 1,
  "startDate": "2025-12-31T00:00:00",
  "endDate": "2026-01-05T23:59:59"
}
```

**Expected:** 
```json
{
  "vehicleTypes": [
    {
      "typeId": 1,
      "typeName": "Honda Vision",
      "totalVehicles": 10,
      "availableCount": 0,
      "availableVehicles": []
    }
  ]
}
```

### Test 4: Invalid dates
**Input:**
```json
{
  "stationId": 1,
  "startDate": "2025-02-10T10:00:00",
  "endDate": "2025-02-05T10:00:00"  // End before start
}
```

**Expected:** 400 Bad Request - "Start date must be before end date"

---

## 🎯 Summary

### Changed
- ❌ Removed `typeId` from request
- ✅ Added `findAvailableVehiclesByStation()` query
- ✅ Group vehicles by type in service layer
- ✅ Return array of `VehicleTypeAvailability`

### Maintained
- ✅ Same conflict detection logic
- ✅ Same date validation
- ✅ Same performance (still using SQL)

### Improved
- ✅ Better UX (1 API call instead of N)
- ✅ More business value (see all options)
- ✅ Easier frontend implementation

---

**Cảm ơn bạn đã phát hiện ra logic sai! Giờ đã đúng business rule rồi!** 🎉
