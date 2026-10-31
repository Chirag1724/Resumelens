# Privacy Policy

**Effective Date:** October 31, 2026  
**Project:** ResumeLens  
**Repository:** https://github.com/Chirag1724/Resumelens  

---

## 1. Overview

ResumeLens is an open-source tool for testing resumes against job descriptions. We describe our exact data handling practices below in plain language.

---

## 2. File Uploads and Stored Data

- Supported files are PDF and DOCX only. We do not support plain text uploads.
- The upload process checks the file extension and the file signature magic bytes.
- The original file is not kept, only the extracted text.
- It is stored so the report link works.
- Anyone with a report link can view that report.

---

## 3. Data Retention and Deletion

- Guest data is deleted after the retention period. A daily scheduled job deletes guest resumes, match results, and section scores older than the configured RETENTION_DAYS (default 7 days).
- Accounts can delete their reports. Deleting a report removes its match result and section scores. When no other match result uses that resume, the resume is deleted as well.

---

## 4. Accounts and Passwords

- Creating an account is optional.
- Passwords are hashed with BCrypt. We never store plain text passwords.
- The login token is kept in memory only. It is not saved to disk or persistent browser storage.

---

## 5. Browser Storage

- The login token is kept in memory and only the cookie consent choice is kept in localStorage.
- The cookie consent choice is stored under `resumelens_cookie_consent_v1`.
- We do not use localStorage keys for user tokens or email addresses.

---

## 6. Hosting Providers and Third Parties

- The hosting and database providers process data on our behalf and may keep their own logs.
- We do not sell your data to third parties.
- We do not use your resume to train AI models.

---

## 7. Code Audit

Our implementation is open source under the MIT License and available at https://github.com/Chirag1724/Resumelens.

---

## 8. Contact

For questions about this policy, contact the maintainer at chiragdwivediji@gmail.com.
