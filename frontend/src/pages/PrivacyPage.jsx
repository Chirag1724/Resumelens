import { Link } from 'react-router-dom';
import { ArrowLeft, Shield, Lock, EyeOff, Database } from 'lucide-react';

export default function PrivacyPage() {
  return (
    <div className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
      {/* Back button */}
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-xs font-medium text-[var(--text-tertiary)] hover:text-[var(--text)] transition-colors mb-8 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] rounded px-1.5 py-1"
      >
        <ArrowLeft className="w-3.5 h-3.5" />
        <span>Back to Home</span>
      </Link>

      {/* Header */}
      <header className="mb-10 pb-8 border-b border-[var(--border)]">
        <div className="flex items-center gap-2 mb-3">
          <span className="inline-flex items-center gap-1.5 text-[11px] font-mono uppercase tracking-[0.08em] px-2.5 py-1 rounded-[6px] bg-[var(--bg-elevated)] border border-[var(--border)] text-[var(--brand-500)]">
            <Shield className="w-3.5 h-3.5" />
            Open Source Privacy
          </span>
          <span className="text-xs font-mono text-[var(--text-tertiary)]">
            Effective: October 31, 2026
          </span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold font-heading text-[var(--text)] tracking-tight">
          Privacy Policy
        </h1>
        <p className="text-sm sm:text-base text-[var(--text-secondary)] mt-3 leading-relaxed max-w-3xl font-body">
          We explain our data practices in plain language. Original files are not kept. Only extracted text is stored so report links work.
        </p>
      </header>

      {/* Quick Guarantees Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-12">
        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-6 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <EyeOff className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              Original File Discarded
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              The original file is not kept on disk. Only the extracted text is saved to generate and display reports.
            </p>
          </div>
        </div>

        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-5 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <Lock className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              Automatic Guest Cleanup
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              Guest data is deleted after the retention period. Resumes, match results, and scores are purged daily.
            </p>
          </div>
        </div>

        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-5 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <Database className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              Account Control
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              Accounts can delete their reports at any time. We do not use your resume to train AI models.
            </p>
          </div>
        </div>
      </div>

      {/* Policy Content Sections */}
      <div className="space-y-10 text-sm text-[var(--text-secondary)] leading-relaxed font-body">
        {/* 1. Document Processing */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">01.</span>
            Document Ingestion and Storage
          </h2>
          <p>
            Supported files are PDF and DOCX only. We check the file extension and the file signature before reading.
          </p>
          <ul className="space-y-2 text-xs sm:text-sm pl-4 list-disc marker:text-[var(--brand-500)]">
            <li>
              The original file is not kept, only the extracted text.
            </li>
            <li>
              It is stored so the report link works.
            </li>
            <li>
              Anyone with a report link can view that report.
            </li>
          </ul>
        </section>

        {/* 2. Retention and Deletion */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">02.</span>
            Data Retention and Deletion
          </h2>
          <p>
            Guest data is deleted after the retention period. A daily scheduled job removes guest resumes, match results, and section scores.
          </p>
          <p>
            Accounts can delete their reports directly from the history dashboard. When you delete a report, its scores and match result are removed. If no other report uses that resume, the resume is also deleted.
          </p>
        </section>

        {/* 3. Account Data */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">03.</span>
            Accounts and Passwords
          </h2>
          <p>
            Creating an account is optional.
          </p>
          <ul className="space-y-2 text-xs sm:text-sm pl-4 list-disc marker:text-[var(--brand-500)]">
            <li>
              Passwords are hashed with BCrypt. We never store plain text passwords.
            </li>
            <li>
              The login token is kept in memory only. It is not saved to persistent browser storage.
            </li>
          </ul>
        </section>

        {/* 4. Local Storage */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">04.</span>
            Browser Storage
          </h2>
          <p>
            The login token is kept in memory and only the cookie consent choice is kept in localStorage.
          </p>
          <p>
            The consent choice is saved under the key <code className="font-mono text-[var(--text)] text-xs">resumelens_cookie_consent_v1</code>. We do not store user tokens or user emails in localStorage.
          </p>
        </section>

        {/* 5. Hosting and Third Parties */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">05.</span>
            Hosting Providers and Service Operation
          </h2>
          <p>
            The hosting and database providers process data on our behalf and may keep their own logs.
          </p>
          <p>
            We do not sell your personal data. We do not use your resume to train AI models.
          </p>
        </section>

        {/* 6. Contact */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">06.</span>
            Open Source Code and Contact
          </h2>
          <p>
            Our source code is open source and available for review at{' '}
            <a
              href="https://github.com/Chirag1724/Resumelens"
              target="_blank"
              rel="noopener noreferrer"
              className="text-[var(--brand-500)] hover:underline font-semibold"
            >
              github.com/Chirag1724/Resumelens
            </a>.
          </p>
          <p>
            For privacy inquiries, contact Chirag Dwivedi at{' '}
            <a
              href="mailto:chiragdwivediji@gmail.com"
              className="text-[var(--brand-500)] hover:underline font-medium"
            >
              chiragdwivediji@gmail.com
            </a>.
          </p>
        </section>
      </div>
    </div>
  );
}
