# Contributing to ResumeLens

Thank you for your interest in contributing to ResumeLens! This project is maintained by **Chirag Dwivedi** ([@Chirag1724](https://github.com/Chirag1724)).

Contributions from the open-source community are welcome. To ensure code quality, reliability, and architectural consistency, please review this guide before submitting contributions.

---

## Code of Conduct

All contributors and participants are expected to adhere to our [Code of Conduct](CODE_OF_CONDUCT.md). Please treat all members of the community with respect and professionalism.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Node.js**: Version 20 or higher (with npm)
- **Docker & Docker Compose**: (Optional, recommended for local PostgreSQL)
- **Git**: Version 2.30+

### Local Development Setup

1. **Fork and Clone**:
   ```bash
   git clone https://github.com/<your-username>/Resumelens.git
   ```

2. **Configure Environment Variables**:
   ```bash
   cp backend/.env.example backend/.env
   cp frontend/.env.example frontend/.env
   ```

3. **Start Database (Docker)**:
   ```bash
   docker compose up postgres -d
   ```

4. **Run Backend (Spring Boot)**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   The backend API will be available at `http://localhost:8080/api/v1`.

5. **Run Frontend (Vite)**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The application will be accessible at `http://localhost:5173`.

---

## Development Workflow

1. **Branch Naming**:
   Create a dedicated branch for your work branching off `main`:
   - Features: `feat/short-description`
   - Bug fixes: `fix/issue-description`
   - Documentation: `docs/topic-name`
   - Refactoring: `refactor/component-name`

2. **Coding Standards**:
   - **Backend**:
     - Follow standard Java naming and formatting conventions.
     - Preserve documentation integrity and architectural boundaries defined in `docs/ARCHITECTURE.md`.
     - Deterministic algorithms in `com.resumelens.algorithm` must not introduce non-deterministic external calls or random seeds.
     - No unhandled runtime exceptions; all business failures should map to structured API responses.
   - **Frontend**:
     - Use React functional components with standard hooks.
     - Styling must adhere to design tokens defined in `src/index.css` and `docs/FRONTEND_DESIGN.md`.
     - Interactive elements must retain keyboard accessibility and semantic ARIA attributes.

3. **Running Tests Before Submitting**:
   Always verify that all backend tests and frontend bundles compile cleanly:
   ```bash
   # Run all backend tests
   cd backend
   ./mvnw test

   # Verify frontend build
   cd ../frontend
   npm run build
   ```

---

## Submitting Pull Requests

1. Push your branch to your personal fork.
2. Open a Pull Request targeting the `main` branch of `Chirag1724/Resumelens`.
3. Provide a clear title and description using the provided Pull Request Template.
4. Ensure all continuous integration checks pass.
5. All pull requests are reviewed by the project owner ([@Chirag1724](https://github.com/Chirag1724)) before merging.

Thank you for helping build a transparent, privacy-first ATS evaluation platform!
