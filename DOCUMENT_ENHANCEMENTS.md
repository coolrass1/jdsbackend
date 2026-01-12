# Document Enhancement Features

This document describes the newly implemented document enhancement features for the Legal Case Management System.

## Features Implemented

### 1. Document Versioning ✅
Track changes to documents over time with full version history.

**Entities:**
- `DocumentVersion` - Stores each version of a document with metadata

**Endpoints:**
- `POST /api/documents/versions/{documentId}` - Create a new version
- `GET /api/documents/versions/{documentId}` - Get all versions of a document
- `GET /api/documents/versions/version/{versionId}` - Get specific version details
- `GET /api/documents/versions/download/{versionId}` - Download a specific version
- `POST /api/documents/versions/{documentId}/restore/{versionNumber}` - Restore a previous version

**Features:**
- Automatic version numbering
- Change descriptions for each version
- Track who uploaded each version
- Restore previous versions
- Download any version

**Example Usage:**
```bash
# Create new version
curl -X POST "http://localhost:8080/api/documents/versions/1" \
  -H "Authorization: Bearer {token}" \
  -F "file=@updated_document.pdf" \
  -F "changeDescription=Fixed typo in section 3"

# Get version history
curl "http://localhost:8080/api/documents/versions/1" \
  -H "Authorization: Bearer {token}"

# Restore version 2
curl -X POST "http://localhost:8080/api/documents/versions/1/restore/2" \
  -H "Authorization: Bearer {token}"
```

---

### 2. Document Preview ✅
View documents directly in the browser without downloading.

**Endpoints:**
- `GET /api/documents/preview/{id}` - Preview document inline (PDFs, images)

**Supported Formats:**
- PDF files
- Image files (PNG, JPG, GIF, etc.)
- Other formats will download instead

**Example Usage:**
```bash
# Preview document in browser
curl "http://localhost:8080/api/documents/preview/1" \
  -H "Authorization: Bearer {token}"
```

**Frontend Integration:**
```html
<!-- PDF Preview -->
<iframe src="http://localhost:8080/api/documents/preview/1" 
        width="100%" height="600px"></iframe>

<!-- Image Preview -->
<img src="http://localhost:8080/api/documents/preview/1" 
     alt="Document Preview" />
```

---

### 3. OCR Capability 📄
Extract text from scanned documents and images.

**Entities:**
- Added `ocrText` field to `Document` entity

**Endpoints:**
- `POST /api/documents/ocr/extract/{documentId}` - Extract text from document
- `GET /api/documents/ocr/{documentId}` - Get extracted OCR text

**Current Implementation:**
- Basic framework in place
- Placeholder for OCR service integration

**To Enable Full OCR:**
Integrate with one of these services:

**Option 1: Tesseract OCR (Open Source)**
```xml
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.7.0</version>
</dependency>
```

**Option 2: Google Cloud Vision API**
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-vision</artifactId>
    <version>3.20.0</version>
</dependency>
```

**Option 3: AWS Textract**
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>textract</artifactId>
    <version>2.20.0</version>
</dependency>
```

**Option 4: Azure Computer Vision**
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-vision-imageanalysis</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

**Example Usage:**
```bash
# Extract text from document
curl -X POST "http://localhost:8080/api/documents/ocr/extract/1" \
  -H "Authorization: Bearer {token}"

# Get extracted text
curl "http://localhost:8080/api/documents/ocr/1" \
  -H "Authorization: Bearer {token}"
```

---

### 4. Document Templates ✅
Pre-defined document templates for common case types.

**Entities:**
- `DocumentTemplate` - Stores reusable document templates

**Endpoints:**
- `POST /api/documents/templates` - Create new template
- `GET /api/documents/templates` - Get all active templates
- `GET /api/documents/templates/category/{category}` - Get templates by category
- `GET /api/documents/templates/{id}` - Get template by ID
- `GET /api/documents/templates/download/{id}` - Download template file
- `PUT /api/documents/templates/{id}` - Update template details
- `PUT /api/documents/templates/{id}/deactivate` - Deactivate template
- `DELETE /api/documents/templates/{id}` - Delete template

**Template Categories:**
- Contract
- Motion
- Affidavit
- Brief
- Petition
- Order
- Notice
- Agreement
- Complaint
- Response

**Example Usage:**
```bash
# Create template
curl -X POST "http://localhost:8080/api/documents/templates" \
  -H "Authorization: Bearer {token}" \
  -F "name=Standard Contract Template" \
  -F "description=Basic contract template for client agreements" \
  -F "category=Contract" \
  -F "file=@contract_template.docx"

# Get all templates
curl "http://localhost:8080/api/documents/templates" \
  -H "Authorization: Bearer {token}"

# Get templates by category
curl "http://localhost:8080/api/documents/templates/category/Contract" \
  -H "Authorization: Bearer {token}"

# Download template
curl "http://localhost:8080/api/documents/templates/download/1" \
  -H "Authorization: Bearer {token}" \
  -o template.docx
```

---

### 5. Digital Signatures ✅
Request and collect digital signatures on documents.

**Entities:**
- `DocumentSignature` - Stores signature requests and signed data
- `DocumentSignatureStatus` - PENDING, SIGNED, REJECTED, EXPIRED

**Endpoints:**
- `POST /api/documents/signatures/request/{documentId}` - Request signature
- `POST /api/documents/signatures/sign` - Sign document (public)
- `POST /api/documents/signatures/reject` - Reject signature request
- `GET /api/documents/signatures/document/{documentId}` - Get all signatures for document
- `GET /api/documents/signatures/pending/{userId}` - Get pending signatures for user
- `GET /api/documents/signatures/token/{token}` - Get signature details by token

**Workflow:**
1. Request signature from a user
2. System generates unique signature token
3. User receives notification with signature link
4. User signs or rejects using the token
5. Signature is recorded with timestamp

**Security:**
- Unique signature tokens for each request
- 7-day expiration on signature requests
- Token-based authentication for signing
- Audit trail of all signature activities

**Example Usage:**
```bash
# Request signature
curl -X POST "http://localhost:8080/api/documents/signatures/request/1" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "signerId": 2,
    "message": "Please review and sign this document"
  }'

# Sign document (public endpoint)
curl -X POST "http://localhost:8080/api/documents/signatures/sign" \
  -H "Content-Type: application/json" \
  -d '{
    "signatureToken": "abc-123-def-456",
    "signatureData": "base64EncodedSignatureImage..."
  }'

# Get pending signatures for user
curl "http://localhost:8080/api/documents/signatures/pending/2" \
  -H "Authorization: Bearer {token}"

# Reject signature
curl -X POST "http://localhost:8080/api/documents/signatures/reject?token=abc-123&reason=Document%20needs%20revision" \
  -H "Content-Type: application/json"
```

---

## Database Schema Changes

### New Tables:
1. **document_versions**
   - Stores version history of documents
   - Links to documents and users

2. **document_templates**
   - Stores reusable document templates
   - Categorized for easy organization

3. **document_signatures**
   - Tracks signature requests and completions
   - Links to documents and signers

### Updated Tables:
**documents** (added fields):
- `current_version` - Current version number
- `description` - Document description
- `ocr_text` - Extracted text from OCR
- `is_template_based` - Whether created from template
- `template_id` - Link to template if used
- `uploaded_by` - User who uploaded
- `updated_at` - Last update timestamp

---

## Configuration

### File Storage Directories

Add to `application.properties`:
```properties
# Document upload directory
file.upload-dir=uploads

# Template storage directory
file.template-dir=templates
```

### Recommended Production Settings

```properties
# Maximum file upload size
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# File storage
file.upload-dir=/var/app/uploads
file.template-dir=/var/app/templates

# OCR Service Configuration (if using cloud service)
ocr.service.provider=google-vision
ocr.service.api-key=${OCR_API_KEY}

# Email notifications for signatures
mail.signature.from=noreply@yourcompany.com
mail.signature.subject-prefix=[Signature Request]
```

---

## Security & Permissions

### Role-Based Access:

**Document Versioning:**
- Create version: CASE_WORKER, ADMIN
- View versions: CASE_WORKER, ADMIN
- Restore version: CASE_WORKER, ADMIN

**Document Preview:**
- View preview: CASE_WORKER, ADMIN

**OCR:**
- Extract text: CASE_WORKER, ADMIN
- View OCR text: CASE_WORKER, ADMIN

**Templates:**
- Create template: ADMIN, MANAGER
- View templates: All authenticated users
- Update template: ADMIN, MANAGER
- Delete template: ADMIN

**Digital Signatures:**
- Request signature: CASE_WORKER, ADMIN, MANAGER
- Sign document: Public (with valid token)
- View signatures: CASE_WORKER, ADMIN, MANAGER

---

## Integration Guidelines

### Frontend Integration

**Document Version History UI:**
```javascript
// Get version history
fetch(`/api/documents/versions/${documentId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(res => res.json())
.then(versions => {
  // Display version timeline
  versions.forEach(v => {
    console.log(`Version ${v.versionNumber}: ${v.changeDescription}`);
  });
});
```

**Document Preview Component:**
```jsx
function DocumentPreview({ documentId }) {
  return (
    <iframe 
      src={`/api/documents/preview/${documentId}`}
      style={{ width: '100%', height: '600px', border: 'none' }}
      title="Document Preview"
    />
  );
}
```

**Signature Workflow:**
```javascript
// Request signature
async function requestSignature(documentId, signerId) {
  const response = await fetch(
    `/api/documents/signatures/request/${documentId}`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ signerId })
    }
  );
  return response.json();
}

// Sign document (signature pad integration)
async function signDocument(token, signatureCanvas) {
  const signatureData = signatureCanvas.toDataURL(); // Base64
  
  const response = await fetch('/api/documents/signatures/sign', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ signatureToken: token, signatureData })
  });
  
  return response.json();
}
```

---

## Future Enhancements

### Potential Improvements:
1. **Advanced OCR:**
   - Multi-language support
   - Table extraction
   - Form field recognition

2. **Enhanced Signatures:**
   - Integration with DocuSign/Adobe Sign
   - Multi-party signatures
   - Signature certificates

3. **Template Features:**
   - Variable placeholders
   - Auto-fill from case data
   - Conditional sections

4. **Document Comparison:**
   - Visual diff between versions
   - Track changes visualization
   - Merge conflict resolution

5. **Collaboration:**
   - Real-time document editing
   - Comment threads
   - Review workflows

---

## Testing

### Test Data Setup:

```bash
# Create a test template
POST /api/documents/templates
{
  "name": "Test Agreement",
  "description": "Sample agreement template",
  "category": "Agreement"
}

# Upload a document
POST /api/documents/upload
caseId=1
file=sample.pdf

# Create a new version
POST /api/documents/versions/1
file=sample_v2.pdf
changeDescription=Updated terms

# Request signature
POST /api/documents/signatures/request/1
{
  "signerId": 2,
  "message": "Please sign"
}

# Extract OCR text
POST /api/documents/ocr/extract/1
```

---

## Support & Documentation

For questions or issues:
1. Check this README
2. Review API endpoint documentation
3. Check logs for error details
4. Contact development team

---

**Version:** 1.0  
**Last Updated:** January 6, 2026  
**Status:** ✅ Implemented and Ready for Testing
