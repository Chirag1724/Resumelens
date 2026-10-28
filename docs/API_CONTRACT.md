# API_CONTRACT.md
> **Authoritative REST API specification for ResumeLens.**
> Generated from the live controller and DTO implementations in `backend/src/main/java/com/resumelens/`.
> All requests and responses adhere to the standard envelope and validation rules defined below.
> Linked to [ARCHITECTURE.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/ARCHITECTURE.md), [FRONTEND_DESIGN.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/FRONTEND_DESIGN.md), and [REQUIREMENTS.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/REQUIREMENTS.md).

---

## 1. Global Standards & Conventions

### 1.1 Base URL & Prefix
- All API routes are rooted at `/api/v1`.
- Content negotiation defaults to `application/json` (unless downloading binary reports where `application/pdf` is returned).

### 1.2 Authentication Scheme
- Stateless **JWT (JSON Web Token)** Bearer authentication.
- Authenticated requests must pass the HTTP header:
  ```http
  Authorization: Bearer <jwt_token>
  ```
- Public/guest endpoints do not require the `Authorization` header. If provided on `/api/v1/resumes/upload`, the resume is automatically linked to the authenticated user's account; if omitted, it is stored as a guest upload.

### 1.3 Standard Response Envelope
All JSON responses are wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

On error, `success` is `false`, `message` contains the error description, and `data` is `null`:

```json
{
  "success": false,
  "message": "Error description message",
  "data": null
}
```

### 1.4 Global HTTP Error Codes
Mapped centrally by `GlobalExceptionHandler`:

| Status Code | Exception Type | Description |
|:---|:---|:---|
| **400 Bad Request** | `InvalidInputException` / `MethodArgumentNotValidException` / `MethodArgumentTypeMismatchException` / `IllegalArgumentException` / `HttpMessageNotReadableException` | Validation failed (missing required field, invalid UUID format, password < 8 chars, empty file, duplicate email). |
| **401 Unauthorized** | `BadCredentialsException` / Spring Security Auth Error | Invalid email or password, or missing/invalid/expired JWT on protected routes. Returns standard ApiResponse JSON body. |
| **403 Forbidden** | Spring Security Access Denied | Access denied to protected resource. Returns standard ApiResponse JSON body. |
| **404 Not Found** | `ResourceNotFoundException` | Specified resource UUID does not exist. Generic message returned without entity class names or IDs. |
| **413 Payload Too Large** | `MaxUploadSizeExceededException` | Uploaded file exceeds maximum allowed limit (10MB). |
| **415 Unsupported Media Type** | `UnsupportedFileTypeException` | Uploaded resume magic bytes do not match PDF (%PDF-) or DOCX (PK 0x03 0x04). |
| **422 Unprocessable Entity** | `ResumeParseException` | File is corrupted or text layer cannot be parsed. |
| **500 Internal Server Error** | `Exception` / `ReportGenerationException` | Unhandled server exception or PDF rendering failure. (Stack traces are never exposed). |

### 1.5 Cross-Origin Resource Sharing (CORS)
- Allowed origins are configured via the `CORS_ALLOWED_ORIGINS` environment variable (comma-separated list, whitespace trimmed).
- Defaults to `http://localhost:5173` if unset. Wildcard origins are rejected.
- Allowed HTTP methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Allowed headers: `*`.
- `Allow-Credentials`: `true`.
- Requests from disallowed origins receive `403 Forbidden` on both preflight (`OPTIONS`) and actual requests to `/api/**`.

---

## 2. Authentication Endpoints (`/api/v1/auth`)

Controller: `com.resumelens.controller.AuthController`

### 2.1 Register User
- **Method:** `POST`
- **Path:** `/api/v1/auth/register`
- **Auth Requirement:** Guest / Public (`permitAll`)
- **Headers:** `Content-Type: application/json`

#### Request Body (`AuthRequest`)
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "name": "Jane Doe"
}
```
| Field | Type | Required | Constraints | Description |
|:---|:---|:---|:---|:---|
| `email` | `String` | Yes | Valid email format, not blank | Unique user email address |
| `password` | `String` | Yes | Min 8 characters, not blank | Raw password (hashed via BCrypt) |
| `name` | `String` | No | Optional | Display name of the user |

#### Success Response (`201 Created`)
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "user@example.com",
    "name": "Jane Doe"
  }
}
```
| Response Field | Type | Description |
|:---|:---|:---|
| `token` | `String` | Signed JWT bearer token |
| `email` | `String` | Registered user email |
| `name` | `String` | User display name |

#### Error Status Codes
- `400 Bad Request`: Validation failure (blank email/password, password < 8 characters) or email already registered (`"An account with this email already exists."`).
- `500 Internal Server Error`: Generic internal server error.

---

### 2.2 Login User
- **Method:** `POST`
- **Path:** `/api/v1/auth/login`
- **Auth Requirement:** Guest / Public (`permitAll`)
- **Headers:** `Content-Type: application/json`

#### Request Body (`AuthRequest`)
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```
| Field | Type | Required | Constraints | Description |
|:---|:---|:---|:---|:---|
| `email` | `String` | Yes | Valid email, not blank | User email |
| `password` | `String` | Yes | Not blank | User password |
| `name` | `String` | No | Ignored during login | Optional field |

#### Success Response (`200 OK`)
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "user@example.com",
    "name": "Jane Doe"
  }
}
```

#### Error Status Codes
- `400 Bad Request`: Malformed payload or validation error.
- `401 Unauthorized`: Invalid credentials (`"Invalid email or password"`).
- `500 Internal Server Error`: Generic internal server error.

---

## 3. Resume Endpoints (`/api/v1/resumes`)

Controller: `com.resumelens.controller.ResumeController`

### 3.1 Upload Resume
- **Method:** `POST`
- **Path:** `/api/v1/resumes/upload`
- **Auth Requirement:** Optional (Guest or Authenticated)
  - If a valid `Authorization: Bearer <token>` header is supplied, the resume is saved linked to that user.
  - If omitted or guest, the resume is saved with `user_id = null`.
- **Headers:** `Content-Type: multipart/form-data`

#### Request Parameters (Multipart Form Fields)
| Parameter | Type | Required | Description |
|:---|:---|:---|:---|
| `file` | `MultipartFile` | Yes | Binary PDF or DOCX file (max 10MB) |
| `targetRole` | `String` | No | Optional target job role hint |
| `experienceYears` | `Integer` | No | Optional candidate experience in years |

#### Success Response (`200 OK`)
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "resumeId": "b3f62821-4f1d-48a5-b3e1-7ca830573e04"
  }
}
```
| Response Field | Type | Description |
|:---|:---|:---|
| `resumeId` | `UUID (String)` | Unguessable public capability UUID of persisted resume |

#### Error Status Codes
- `400 Bad Request`: File is missing or empty (`"Uploaded file is empty."`).
- `413 Payload Too Large`: Uploaded file exceeds maximum allowed limit (10MB).
- `415 Unsupported Media Type`: File signature does not match PDF or DOCX (`"Unsupported file type. Only PDF and DOCX are supported."`).
- `422 Unprocessable Entity`: Corrupt file or text extraction failure.
- `500 Internal Server Error`: Server failure during processing.

---

## 4. Analysis Endpoints (`/api/v1/analysis`)

Controller: `com.resumelens.controller.AnalysisController`

### Capability URL & Guest Access Model
All analysis and report retrieval endpoints use unguessable UUIDs as public capabilities. In line with the guest access model ("anyone with the link can view"), possession of the valid public UUID grants read access to the match result, PDF report, and fix-prompt without requiring authentication. Numeric IDs remain strictly internal and are never exposed in URLs or responses.

### 4.1 Run Analysis / Match
- **Method:** `POST`
- **Path:** `/api/v1/analysis/match`
- **Auth Requirement:** Guest / Public (`permitAll`)
- **Headers:** `Content-Type: application/json`

#### Request Body (`AnalysisRequest`)
```json
{
  "resumeId": "b3f62821-4f1d-48a5-b3e1-7ca830573e04",
  "jobDescriptionId": null,
  "rawJobDescription": "We are looking for a Senior Java Developer with Spring Boot, Docker, and Kubernetes experience...",
  "jobTitle": "Senior Java Developer"
}
```
| Field | Type | Required | Description |
|:---|:---|:---|:---|
| `resumeId` | `UUID (String)` | Yes (`@NotNull`) | Public UUID of the previously uploaded resume |
| `jobDescriptionId` | `Long` | Conditional | ID of an existing JD (provide this OR `rawJobDescription`) |
| `rawJobDescription` | `String` | Conditional | Plain text JD (provide this OR `jobDescriptionId`) |
| `jobTitle` | `String` | No | Optional job title label used in prompts and reports |

#### Success Response (`200 OK`)
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "e2a394c8-3c4a-4467-8ef0-9bb499b70b4a",
    "fileName": "resume.pdf",
    "analyzedAt": "2026-10-26T14:30:00.123456",
    "targetRole": "Senior Java Developer",
    "experienceYears": 5,
    "overallScore": 78.45,
    "statusLabel": "Strong Match",
    "atsScore": 82.0,
    "skillsScore": 75.5,
    "impactScore": 80.0,
    "projectScore": 75.0,
    "writingScore": 85.0,
    "professionalismScore": 90.0,
    "keywordMatchPercent": 68.18,
    "sectionScores": [
      {
        "sectionName": "ATS_COMPATIBILITY",
        "score": 82.0,
        "status": "STRONG",
        "feedback": "ATS formatting and keyword compliance",
        "matchedKeywords": "[]",
        "missingKeywords": "[]"
      },
      {
        "sectionName": "SKILLS_VALIDATION",
        "score": 75.5,
        "status": "STRONG",
        "feedback": "Keyword and skill match against job description",
        "matchedKeywords": "[\"java\", \"spring boot\", \"docker\"]",
        "missingKeywords": "[\"kubernetes\", \"microservices\"]"
      },
      {
        "sectionName": "IMPACT_ACHIEVEMENTS",
        "score": 80.0,
        "status": "STRONG",
        "feedback": "Quantified achievements and action verb usage",
        "matchedKeywords": "[]",
        "missingKeywords": "[]"
      },
      {
        "sectionName": "PROJECT_QUALITY",
        "score": 75.0,
        "status": "STRONG",
        "feedback": "Project section completeness and measurable outcomes",
        "matchedKeywords": "[]",
        "missingKeywords": "[]"
      },
      {
        "sectionName": "WRITING_QUALITY",
        "score": 85.0,
        "status": "EXCELLENT",
        "feedback": "Sentence length, first-person usage, filler words, passive voice",
        "matchedKeywords": "[]",
        "missingKeywords": "[]"
      },
      {
        "sectionName": "PROFESSIONALISM",
        "score": 90.0,
        "status": "EXCELLENT",
        "feedback": "Date consistency, bullet style, email quality, resume length",
        "matchedKeywords": "[]",
        "missingKeywords": "[]"
      }
    ],
    "suggestions": [
      {
        "priority": "HIGH",
        "title": "Add Missing Technical Keywords",
        "description": "Incorporate the following missing keywords from the job description: kubernetes, microservices.",
        "example": "Led migration of microservices architecture using Kubernetes clusters."
      },
      {
        "priority": "MEDIUM",
        "title": "Strengthen Action Verbs",
        "description": "Replace weak or generic verbs (e.g. helped, worked on) with strong active impact verbs.",
        "example": "Engineered, Spearheaded, Architected."
      }
    ],
    "risks": [
      {
        "title": "No Contact Info",
        "severity": "MEDIUM",
        "description": "No email address or phone number was found on the resume.",
        "fix": "Include your email address and phone number at the top of your resume."
      }
    ],
    "improvementPrompt": "LaTeX improvement prompt..."
  }
}
```

#### Field Types & Value Spaces
| Field | Type | Range / Format | Description |
|:---|:---|:---|:---|
| `id` | `UUID (String)` | UUID string | Unique unguessable capability identifier of this match result |
| `fileName` | `String` | Text | Original file name of the analyzed resume |
| `analyzedAt` | `LocalDateTime (ISO-8601)` | Timestamp | Timestamp when the analysis was performed |
| `targetRole` | `String` | Text / `null` | Target role provided during upload (`null` when absent) |
| `experienceYears` | `Integer` | Integer / `null` | Candidate experience in years provided during upload (`null` when absent) |
| `overallScore` | `Double` | `0.00` - `100.00` | Weighted aggregate score |
| `statusLabel` | `String` | `"Excellent Match"` \| `"Strong Match"` \| `"Moderate Match"` \| `"Weak Match"` \| `"Poor Match"` | Qualitative score tier |
| `atsScore` | `Double` | `0.0` - `100.0` | ATS formatting dimension score (25%) |
| `skillsScore` | `Double` | `0.0` - `100.0` | TF-IDF + Cosine similarity skill score (25%) |
| `impactScore` | `Double` | `0.0` - `100.0` | Quantified achievements score (20%) |
| `projectScore` | `Double` | `0.0` - `100.0` | Project section completeness score (15%) |
| `writingScore` | `Double` | `0.0` - `100.0` | Grammar & writing style score (10%) |
| `professionalismScore` | `Double` | `0.0` - `100.0` | Length & formatting consistency score (5%) |
| `keywordMatchPercent` | `Double` | `0.0` - `100.0` | Raw percentage of JD keywords matched |
| `sectionScores` | `List<SectionScoreResponse>` | 6 items | Dimension breakdown (`status`: `"EXCELLENT"` \| `"STRONG"` \| `"MODERATE"` \| `"WEAK"` \| `"POOR"`) |
| `suggestions` | `List<SuggestionResponse>` | 0-5 items | Top actionable improvement recommendations |
| `risks` | `List<RiskResponse>` | 0-16 items | Scoring risks derived from triggered rules, ordered by severity (`HIGH`, `MEDIUM`, `LOW`) |
| `improvementPrompt` | `String` | Formatted text | Copyable prompt formatted for external AI assistant |

##### Risk Item Structure (`RiskResponse`)
| Field | Type | Range / Format | Description |
|:---|:---|:---|:---|
| `title` | `String` | Text | Human-readable title of the fired rule |
| `severity` | `String` | `"HIGH"` \| `"MEDIUM"` \| `"LOW"` | Derived from deduction size: >=15 is HIGH, 8–14 is MEDIUM, <8 is LOW |
| `description` | `String` | Plain sentence | Short description explaining the risk (no dynamic numbers) |
| `fix` | `String` | Plain sentence | Actionable guidance explaining how to resolve the risk |

#### Error Status Codes
- `400 Bad Request`: `resumeId` is null, invalid UUID format, or neither `jobDescriptionId` nor `rawJobDescription` was supplied.
- `404 Not Found`: `resumeId` or `jobDescriptionId` does not exist.
- `500 Internal Server Error`: Processing failure during scoring or prompt generation.

---

### 4.2 Get Analysis Result
- **Method:** `GET`
- **Path:** `/api/v1/analysis/{id}`
- **Auth Requirement:** Guest / Public (`permitAll`)

#### Path Parameters
| Parameter | Type | Required | Description |
|:---|:---|:---|:---|
| `id` | `UUID (String)` | Yes | Public capability UUID of the match result to fetch |

#### Success Response (`200 OK`)
Returns `ApiResponse<AnalysisResponse>` with identical structure and contents as `POST /api/v1/analysis/match` (metadata fields `fileName`, `analyzedAt`, `targetRole`, and `experienceYears` return identical values; suggestions and risks are deterministically recomputed from the persisted resume and job description).

#### Error Status Codes
- `400 Bad Request`: Invalid UUID format.
- `404 Not Found`: No match result exists with UUID `{id}`.
- `500 Internal Server Error`: Generic internal server error.

---

### 4.3 Download PDF Report
- **Method:** `GET`
- **Path:** `/api/v1/analysis/{id}/report`
- **Auth Requirement:** Guest / Public (`permitAll`)
- **Headers Returned:**
  - `Content-Type: application/pdf`
  - `Content-Disposition: attachment; filename="ats-report-{id}.pdf"`

#### Path Parameters
| Parameter | Type | Required | Description |
|:---|:---|:---|:---|
| `id` | `UUID (String)` | Yes | Public capability UUID of the match result |

#### Success Response (`200 OK`)
- **Body:** Raw binary byte array (`byte[]`) containing the generated iText PDF document.

#### Error Status Codes
- `400 Bad Request`: Invalid UUID format.
- `404 Not Found`: Match result with UUID `{id}` does not exist.
- `500 Internal Server Error`: PDF generation failed (`ReportGenerationException`).

---

### 4.4 Get Copyable Fix-Prompt
- **Method:** `GET`
- **Path:** `/api/v1/analysis/{id}/fix-prompt`
- **Auth Requirement:** Guest / Public (`permitAll`)

#### Path Parameters
| Parameter | Type | Required | Description |
|:---|:---|:---|:---|
| `id` | `UUID (String)` | Yes | Public capability UUID of the match result |

#### Query Parameters
| Parameter | Type | Required | Default | Description |
|:---|:---|:---|:---|:---|
| `templateStyle` | `String` | No | `"Simple Single-Column"` | Desired LaTeX template layout style |

#### Success Response (`200 OK`)
```json
{
  "success": true,
  "message": "OK",
  "data": "You are a professional resume editor. Rewrite the candidate's resume in LaTeX format targeting the position of Senior Java Developer...\n[Anti-Fabrication Rules]\n- Do NOT invent experiences...\n[Template: Simple Single-Column]\n..."
}
```

#### Error Status Codes
- `400 Bad Request`: Invalid UUID format.
- `404 Not Found`: Match result does not exist.
- `500 Internal Server Error`: Failure assembling prompt.

---

## 5. History Endpoints (`/api/v1/history`)

Controller: `com.resumelens.controller.HistoryController`

### 5.1 Get User Analysis History
- **Method:** `GET`
- **Path:** `/api/v1/history`
- **Auth Requirement:** Authenticated (`.requestMatchers("/api/v1/history/**").authenticated()`)
- **Headers:** `Authorization: Bearer <jwt_token>`

#### Success Response (`200 OK`)
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": "e2a394c8-3c4a-4467-8ef0-9bb499b70b4a",
      "resumeId": "b3f62821-4f1d-48a5-b3e1-7ca830573e04",
      "resumeFileName": "jane_doe_resume.pdf",
      "jobTitle": "Senior Java Developer",
      "overallScore": 78.45,
      "statusLabel": "Strong Match",
      "analyzedAt": "2026-10-15T22:30:00"
    },
    {
      "id": "7bc932a1-12a8-4449-9df1-801211ef5123",
      "resumeId": "b3f62821-4f1d-48a5-b3e1-7ca830573e04",
      "resumeFileName": "jane_doe_resume.pdf",
      "jobTitle": "Cloud Infrastructure Lead",
      "overallScore": 64.20,
      "statusLabel": "Moderate Match",
      "analyzedAt": "2026-10-14T11:15:00"
    }
  ]
}
```
| Response Field | Type | Description |
|:---|:---|:---|
| `id` | `UUID (String)` | Unguessable public capability UUID of the analysis match result |
| `resumeId` | `UUID (String)` | Unguessable public capability UUID of the analyzed resume |
| `resumeFileName` | `String` | Original name of uploaded resume file |
| `jobTitle` | `String` | Target job title |
| `overallScore` | `Double` | Overall score (0-100) |
| `statusLabel` | `String` | Status label tier |
| `analyzedAt` | `String (ISO-8601)` | Timestamp when analysis was run |

#### Ordering & Pagination
- Results are strictly sorted in descending chronological order (`analyzedAt DESC`).
- Returns only the latest 50 results via PageRequest.

#### Error Status Codes
- `401 Unauthorized` / `403 Forbidden`: Request is unauthenticated or token is expired/invalid.
- `500 Internal Server Error`: Generic internal server error.

---

## 6. Health Endpoint (`/api/v1/health`)

Controller: `com.resumelens.controller.HealthController`

### 6.1 Liveness & Health Probe
- **Method:** `GET`
- **Path:** `/api/v1/health`
- **Auth Requirement:** Guest / Public (`permitAll`)

#### Success Response (`200 OK`)
```json
{
  "status": "UP"
}
```

---

## 7. Frontend Integration Checklist

1. **Base URL configuration:** Set Axios `baseURL` to `/api/v1` (or `http://localhost:8080/api/v1` in local development).
2. **Guest upload flow:** Call `POST /resumes/upload` with FormData `file`, extract UUID `data.resumeId`, then call `POST /analysis/match` with `{ resumeId, rawJobDescription, jobTitle }`. Store match result UUID `id` in client state.
3. **Report & Fix Prompt:** Fetch PDF binary from `GET /analysis/{id}/report` with `responseType: 'blob'`, and prompt from `GET /analysis/{id}/fix-prompt`.
4. **Auth tokens:** Store JWT from `POST /auth/login` or `POST /auth/register` in `localStorage` or in-memory auth state, and attach via Axios request interceptor.
