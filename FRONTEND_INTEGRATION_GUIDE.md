# 📸 Hướng dẫn tích hợp API Upload hình ảnh - Frontend

## 🔗 API Endpoints đã được update

### 1. Upload Document (CCCD, GPLX)
```
POST /api/documents
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**Request Body (FormData):**
```javascript
{
  userId: number,
  documentType: "CMND" | "CCCD" | "PASSPORT" | "DRIVING_LICENSE",
  documentNumber: string,
  frontPhoto: File,      // ✅ Image file
  backPhoto: File,       // ✅ Image file
  issueDate: string (ISO),
  expiryDate: string (ISO),
  issuedBy: string,
  isDefault: boolean
}
```

### 2. Upload Return Transaction Photos
```
POST /api/return-transactions
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**Request Body (FormData):**
```javascript
{
  bookingId: string,
  conditionNotes: string,
  photos: File[],        // ✅ Multiple image files
  damageFee: number
}
```

---

## 💻 Frontend Integration Examples

### React/TypeScript Example

#### 1. Upload Document
```typescript
// DocumentUpload.tsx
import axios from 'axios';

const uploadDocument = async (formData: {
  userId: number;
  documentType: string;
  documentNumber: string;
  frontPhoto: File | null;
  backPhoto: File | null;
  issueDate: string;
  expiryDate: string;
  issuedBy: string;
  isDefault: boolean;
}) => {
  try {
    // Create FormData
    const data = new FormData();
    data.append('userId', formData.userId.toString());
    data.append('documentType', formData.documentType);
    data.append('documentNumber', formData.documentNumber);
    
    if (formData.frontPhoto) {
      data.append('frontPhoto', formData.frontPhoto);
    }
    
    if (formData.backPhoto) {
      data.append('backPhoto', formData.backPhoto);
    }
    
    data.append('issueDate', formData.issueDate);
    data.append('expiryDate', formData.expiryDate);
    data.append('issuedBy', formData.issuedBy);
    data.append('isDefault', formData.isDefault.toString());

    // Upload
    const response = await axios.post('/api/documents', data, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / (progressEvent.total || 1)
        );
        console.log(`Upload Progress: ${percentCompleted}%`);
      }
    });

    console.log('Document uploaded:', response.data);
    return response.data;
  } catch (error) {
    console.error('Upload failed:', error);
    throw error;
  }
};

// Component example
const DocumentUploadForm = () => {
  const [frontPhoto, setFrontPhoto] = useState<File | null>(null);
  const [backPhoto, setBackPhoto] = useState<File | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    await uploadDocument({
      userId: 123,
      documentType: 'CCCD',
      documentNumber: '001234567890',
      frontPhoto,
      backPhoto,
      issueDate: '2020-01-01T00:00:00Z',
      expiryDate: '2030-01-01T00:00:00Z',
      issuedBy: 'Công an TP.HCM',
      isDefault: true
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="file"
        accept="image/*"
        onChange={(e) => setFrontPhoto(e.target.files?.[0] || null)}
      />
      <input
        type="file"
        accept="image/*"
        onChange={(e) => setBackPhoto(e.target.files?.[0] || null)}
      />
      <button type="submit">Upload Document</button>
    </form>
  );
};
```

#### 2. Upload Return Transaction Photos
```typescript
// ReturnTransactionUpload.tsx
const uploadReturnTransaction = async (formData: {
  bookingId: string;
  conditionNotes: string;
  photos: File[];
  damageFee?: number;
}) => {
  try {
    const data = new FormData();
    data.append('bookingId', formData.bookingId);
    data.append('conditionNotes', formData.conditionNotes);
    
    // Append multiple photos
    formData.photos.forEach((photo) => {
      data.append('photos', photo);
    });
    
    if (formData.damageFee) {
      data.append('damageFee', formData.damageFee.toString());
    }

    const response = await axios.post('/api/return-transactions', data, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    console.log('Return transaction created:', response.data);
    return response.data;
  } catch (error) {
    console.error('Upload failed:', error);
    throw error;
  }
};

// Component example
const ReturnTransactionForm = () => {
  const [photos, setPhotos] = useState<File[]>([]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setPhotos(Array.from(e.target.files));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    await uploadReturnTransaction({
      bookingId: 'BK123456',
      conditionNotes: 'Vehicle returned in good condition',
      photos
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="file"
        accept="image/*"
        multiple
        onChange={handleFileChange}
      />
      <p>Selected: {photos.length} file(s)</p>
      <button type="submit">Submit Return</button>
    </form>
  );
};
```

### Vue.js Example

```vue
<template>
  <div>
    <h3>Upload Document</h3>
    <form @submit.prevent="uploadDocument">
      <input 
        type="file" 
        @change="handleFrontPhoto" 
        accept="image/*"
      />
      <input 
        type="file" 
        @change="handleBackPhoto" 
        accept="image/*"
      />
      <button type="submit">Upload</button>
    </form>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      frontPhoto: null,
      backPhoto: null
    };
  },
  methods: {
    handleFrontPhoto(e) {
      this.frontPhoto = e.target.files[0];
    },
    handleBackPhoto(e) {
      this.backPhoto = e.target.files[0];
    },
    async uploadDocument() {
      const formData = new FormData();
      formData.append('userId', 123);
      formData.append('documentType', 'CCCD');
      formData.append('documentNumber', '001234567890');
      
      if (this.frontPhoto) {
        formData.append('frontPhoto', this.frontPhoto);
      }
      if (this.backPhoto) {
        formData.append('backPhoto', this.backPhoto);
      }

      try {
        const response = await axios.post('/api/documents', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        console.log('Success:', response.data);
      } catch (error) {
        console.error('Error:', error);
      }
    }
  }
};
</script>
```

---

## 📋 Validation Rules

### File Upload Constraints:
- ✅ **Max file size**: 10MB per file
- ✅ **Accepted formats**: Image files only (image/jpeg, image/png, image/jpg, etc.)
- ✅ **Multiple files**: ReturnTransaction accepts multiple photos

### Error Responses:
```json
{
  "status": "error",
  "message": "File size exceeds maximum limit (10MB)"
}

{
  "status": "error",
  "message": "Only image files are allowed"
}

{
  "status": "error",
  "message": "File is empty"
}
```

---

## 🔒 Security Notes

1. **Authentication**: All endpoints require Bearer token
2. **Authorization**: 
   - Documents: STAFF, ADMIN, CUSTOMER
   - Return Transactions: STAFF, ADMIN only
3. **File validation**: Backend validates file type and size
4. **Cloud storage**: Files stored on Cloudinary (secure)

---

## 📷 Image Display

Khi nhận response từ API, URLs sẽ có dạng:
```
https://res.cloudinary.com/duklfdbqf/image/upload/v123/ev-rental/documents/uuid_filename.jpg
```

Hiển thị hình ảnh:
```jsx
<img src={documentResponse.frontPhoto} alt="Front Photo" />
<img src={documentResponse.backPhoto} alt="Back Photo" />

// Return transaction photos (comma-separated)
{returnTransactionResponse.photos?.split(',').map((url, index) => (
  <img key={index} src={url} alt={`Photo ${index + 1}`} />
))}
```

---

## ✅ Testing với Postman

### Upload Document
```
POST http://localhost:8080/api/documents
Headers:
  - Authorization: Bearer <your-token>
  - Content-Type: multipart/form-data

Body (form-data):
  - userId: 1
  - documentType: CCCD
  - documentNumber: 001234567890
  - frontPhoto: [Select file]
  - backPhoto: [Select file]
  - issueDate: 2020-01-01T00:00:00Z
  - expiryDate: 2030-01-01T00:00:00Z
  - issuedBy: Công an TP.HCM
  - isDefault: true
```

### Upload Return Transaction
```
POST http://localhost:8080/api/return-transactions
Headers:
  - Authorization: Bearer <your-token>
  - Content-Type: multipart/form-data

Body (form-data):
  - bookingId: BK123456
  - conditionNotes: Good condition
  - photos: [Select multiple files]
  - damageFee: 0
```

---

## 🎯 Summary

✅ Backend đã sẵn sàng nhận file upload
✅ Cloudinary đã được cấu hình
✅ API endpoints đã update
✅ Validation đã được implement
✅ Security đã được đảm bảo

**Frontend chỉ cần:**
1. Tạo FormData object
2. Append files vào FormData
3. Gửi request với Content-Type: multipart/form-data
4. Nhận URLs từ response để hiển thị

**Happy coding! 🚀**
