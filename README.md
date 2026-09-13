# ResumeLens

ATS resume scoring tool that evaluates a resume against a job description using TF-IDF, Cosine Similarity, and Levenshtein Distance, all implemented from scratch in Java.

> Screenshots will be added once the frontend is complete.

---

## What it does

ResumeLens accepts a resume (PDF or plain text) and a job description, then produces a structured compatibility score broken down across six categories: ATS formatting compliance, skills match, impact and achievements, project quality, writing quality, and professionalism. The score reflects how well a resume is likely to perform against an automated screening system for that specific role. Alongside the score, the system generates a copyable prompt the user can take to an external AI assistant (such as ChatGPT or Gemini) to get specific improvement suggestions, the system itself does not call any AI API.

---

## Features

- Resume parsing from PDF and plain text input
- TF-IDF weighted keyword extraction from the job description
- Cosine Similarity scoring between resume and job description term vectors
- Levenshtein Distance fuzzy matching for skill name variants
- Six-category section scoring with per-section feedback and weighted overall score
- Keyword gap analysis listing matched and missing JD keywords
- ATS risk detection covering formatting, missing sections, and contact info issues
- Prioritized improvement suggestions based on lowest-scoring sections
- PDF report export of the full analysis result
- Optional account creation with saved analysis history
- Fix-prompt generation: a structured, copyable prompt for use with an external AI assistant

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + Vite, plain CSS |
| Backend | Spring Boot (Java 17+), Spring Data JPA |
| Database | MySQL 8+ |
| Authentication | JWT via Spring Security |
| PDF parsing | Apache PDFBox |
| DOCX parsing | Apache POI |
| Report generation | iText (PDF export) |

---

## How It Works

1. The user uploads a resume (PDF or pastes plain text) and provides a job description.
2. The backend extracts and normalises text from the resume using PDFBox or POI depending on file type.
3. TF-IDF is computed over the job description to identify the most important keywords, capped at 30 terms.
4. Each JD keyword is checked against the resume using exact matching and Levenshtein fuzzy matching with a distance threshold of 2.
5. A Cosine Similarity score is computed between the resume and JD term-frequency vectors.
6. Six scoring dimensions are evaluated independently and combined using fixed weights into an overall score between 0 and 100.
7. The system returns a full breakdown: per-section scores, matched and missing keywords, ATS risk flags, improvement notes, and a copyable AI prompt.

---

## Project Structure

```
backend/    Spring Boot REST API, scoring logic, and self-implemented NLP algorithms
frontend/   React + Vite single-page application
docs/       Architecture documentation, scoring rules, conventions, and decisions log
```

---

## Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.8 or later
- Node.js 18 or later
- MySQL 8 running locally

### Backend Setup

1. Clone the repository.

   ```bash
   git clone https://github.com/Chirag1724/Resumelens.git
   cd Resumelens
   ```

2. Create a MySQL database named `resumelens`.

   ```sql
   CREATE DATABASE resumelens CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. Set the required environment variables. Database credentials are never hardcoded in the committed configuration files.

   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/resumelens
   export DB_USERNAME=your_mysql_user
   export DB_PASSWORD=your_mysql_password
   export JWT_SECRET=a_long_random_secret_string
   ```

4. Run the backend.

   ```bash
   cd backend
   mvn spring-boot:run
   ```

   The API will be available at `http://localhost:8080`. The health endpoint at `/api/v1/health` returns `{ "status": "UP" }` when the server is ready.

### Frontend Setup

1. Navigate to the frontend directory.

   ```bash
   cd frontend
   ```

2. Install dependencies.

   ```bash
   npm install
   ```

3. Copy the environment template and set the API base URL.

   ```bash
   cp .env.example .env.local
   # Edit .env.local and set VITE_API_BASE_URL=http://localhost:8080
   ```

4. Start the development server.

   ```bash
   npm run dev
   ```

   The frontend will be available at `http://localhost:5173`.

---

## Running Tests

```bash
cd backend
mvn test
```

---

## Algorithms

### TF-IDF

Term Frequency-Inverse Document Frequency is used to weight keywords in the job description by how informative they are relative to the resume text. Implementing it from scratch rather than delegating to a library such as Apache Lucene or Elasticsearch means the exact weighting formula is visible, auditable, and free of index-management overhead that is unnecessary for a two-document comparison.

### Cosine Similarity

Cosine Similarity measures the angle between the resume and job description as TF-IDF weighted term vectors, producing a score that is independent of document length. A from-scratch implementation avoids pulling in a full linear algebra or NLP library for what reduces to a dot product and two magnitude calculations, keeping the dependency footprint minimal and the logic transparent.

### Levenshtein Distance

Levenshtein Distance counts the minimum single-character edits needed to transform one string into another, used here to match skill name variants such as "Javascript" against "JavaScript" or "Node" against "Node.js". Implementing it directly in Java allows the edit-distance threshold (currently 2) to be tuned and documented in one place, with no licence or version compatibility concerns from an external library.

---

## Limitations

- Matching is keyword-level, not semantic. Synonyms or paraphrased descriptions of the same skill are not recognised unless they are within the Levenshtein threshold.
- English-only. The text normalisation and keyword extraction pipeline is not designed or tested for non-English resumes or job descriptions.
- Resume content is not verified. The system scores what is written, with no way to validate whether stated experience or skills are accurate.
- Scoring weights are manually specified based on informed judgment, not derived from a large validated dataset. The relative importance of each dimension may not generalise to all industries or roles.

---

## Roadmap

- Live job portal integration to pull job descriptions directly from public listings
- ML-based resume classification to adjust scoring criteria by industry or role type
- Recruiter-facing dashboard for reviewing and filtering candidate submissions
- Multi-language support for resume and job description processing
- Automated AI-assisted resume rewriting integrated directly into the results view

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
