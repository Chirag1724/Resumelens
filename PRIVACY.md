# Privacy Policy

**Effective Date:** October 30, 2026  
**Project:** ResumeLens (Open-Source ATS Resume Intelligence Engine)  
**Maintainer:** Chirag Dwivedi ([@Chirag1724](https://github.com/Chirag1724))  
**Repository:** [https://github.com/Chirag1724/Resumelens](https://github.com/Chirag1724/Resumelens)

---

## 1. Overview and Core Philosophy

ResumeLens is built on a **privacy-first, ephemeral processing** architecture. We believe that job seekers should not have to surrender their personal information, employment history, contact details, or intellectual property just to evaluate how their resume performs against automated Applicant Tracking Systems (ATS).

Our source code is publicly auditable under the MIT License. This policy transparently outlines how data is handled during resume evaluation, user authentication, and system operation.

---

## 2. Information We Process

### 2.1 Resume Documents and Job Descriptions
- **File Uploads:** When you upload a resume in PDF, DOCX, or plain text format, our backend service (Spring Boot 3) extracts raw text in memory using Apache PDFBox and Apache POI.
- **Job Descriptions:** When you submit job description text, it is processed in memory to compute TF-IDF terms, keyword frequency matrices, and cosine similarity.
- **Ephemeral Processing Guarantee:**
  - For guest evaluations (unauthenticated users), uploaded documents and parsed text are evaluated strictly in RAM and are **never** persisted to disk, relational databases, object storage, or external logs.
  - No copies of your uploaded resume files are retained after the HTTP response is completed.
  - For authenticated users who explicitly choose to save an evaluation, only the computed score metadata, section percentages, and matched keyword summaries are saved to the user's private history. The original raw binary resume file is not stored.

### 2.2 Account and Authentication Data
- If you register for an optional account:
  - **Email Address:** Stored in the database for identification and session recovery.
  - **Password:** Passwords are never stored in plaintext. They are cryptographically hashed using BCrypt before storage.
  - **Authentication Tokens:** We issue JSON Web Tokens (JWT) signed with a private secret. These tokens are stored client-side in browser local storage and expire automatically.

### 2.3 Device and Browser Information
- Basic HTTP request metadata (such as User-Agent and IP address) may be briefly processed by web server networking layers for rate limiting and denial-of-service protection. These are not linked to your resume content or sold to any third party.

---

## 3. What We Do NOT Do

- **We do NOT sell data:** We never sell, rent, monetize, or broker your personal information, resume content, contact info, or email address.
- **We do NOT train proprietary commercial AI models on your resume:** Your uploaded resumes are not harvested to train commercial large language models.
- **We do NOT share data with recruiters or employers:** ResumeLens is an independent candidate-empowerment tool. Evaluations are private to you.
- **We do NOT use third-party advertising trackers:** We do not embed Google AdSense, Meta Pixel, or third-party behavioral tracking scripts.

---

## 4. Local Storage and Cookies

ResumeLens uses minimal, essential client-side storage:

| Storage Key | Type | Purpose | Duration |
| :--- | :--- | :--- | :--- |
| `resumelens_token` | Local Storage | Maintains authenticated user session (JWT). | Until logout or expiry |
| `resumelens_user_email` | Local Storage | Displays current user profile in the top navigation. | Until logout |
| `resumelens_cookie_consent_v1` | Local Storage | Stores user choice for site preferences (`accepted`, `essential`, `dismissed`). | 365 days or until cleared |

You can inspect, manage, or clear these keys at any time via your browser developer tools or the **Cookie Preferences** controls in the site footer.

---

## 5. Data Security Measures

- **In-Memory Text Extraction:** Documents are parsed into temporary memory buffers and discarded immediately after scoring.
- **TLS/HTTPS Encryption:** All data in transit between the client browser and the ResumeLens API is encrypted using Transport Layer Security (TLS 1.3).
- **Password Hashing:** Passwords are salted and hashed with BCrypt.
- **Database Isolation:** Database queries use parameterized queries (Spring Data JPA / Hibernate) to prevent injection attacks.
- **Parser Hardening:** Document intake routines enforce strict file size limits (10MB max) and MIME type validation to defend against denial-of-service and parser exploit payloads.

---

## 6. User Rights and Data Deletion

You maintain complete ownership of your personal data:
- **Guest Users:** Since no resume documents or personal data are stored for guest scans, there is no stored data to delete.
- **Registered Users:** You have the right to request full deletion of your account and associated score history at any time. You can clear individual reports from your history dashboard or contact the maintainer to purge all associated database records.

---

## 7. Open Source Transparency

Because ResumeLens is open source, our data handling claims can be independently audited by inspecting the codebase:
- Document Parser: [`backend/src/main/java/com/resumelens/service/ResumeParserService.java`](https://github.com/Chirag1724/Resumelens/blob/main/backend/src/main/java/com/resumelens/service/ResumeParserService.java)
- Scoring Engine: [`backend/src/main/java/com/resumelens/service/ATSScoreEngine.java`](https://github.com/Chirag1724/Resumelens/blob/main/backend/src/main/java/com/resumelens/service/ATSScoreEngine.java)
- Authentication Controller: [`backend/src/main/java/com/resumelens/controller/AuthController.java`](https://github.com/Chirag1724/Resumelens/blob/main/backend/src/main/java/com/resumelens/controller/AuthController.java)

---

## 8. Changes to This Policy

If this privacy policy is modified, changes will be committed directly to the Git repository with a corresponding version tag or commit message. Significant revisions will be reflected in the updated date at the top of this document.

---

## 9. Contact and Inquiries

If you have questions, privacy concerns, or security disclosures regarding this policy:
- **Maintainer:** Chirag Dwivedi
- **Email:** chiragdwivediji@gmail.com
- **GitHub Issues:** [https://github.com/Chirag1724/Resumelens/issues](https://github.com/Chirag1724/Resumelens/issues)
- **Security Disclosures:** Refer to [SECURITY.md](SECURITY.md)
