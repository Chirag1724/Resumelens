import { Link } from 'react-router-dom';
import { ArrowLeft, Shield, Lock, EyeOff, Database, FileCheck, CheckCircle2 } from 'lucide-react';

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
            Effective: October 30, 2026
          </span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold font-heading text-[var(--text)] tracking-tight">
          Privacy Policy
        </h1>
        <p className="text-sm sm:text-base text-[var(--text-secondary)] mt-3 leading-relaxed max-w-3xl font-body">
          ResumeLens is built on an ephemeral, privacy-first architecture. We process resume text in memory to diagnose ATS compatibility without storing raw files, tracking personal identity, or monetizing your career data.
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
              Zero Raw File Persistence
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              Uploaded resumes are parsed in memory and immediately discarded. No disk copies are kept.
            </p>
          </div>
        </div>

        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-5 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <Lock className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              No Data Brokering
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              We never sell or share resumes with recruiters, advertisers, or third-party brokers.
            </p>
          </div>
        </div>

        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-5 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <Database className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              No AI Model Training
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              Your resume is not harvested or utilized to train commercial LLMs or external models.
            </p>
          </div>
        </div>
      </div>

      {/* Policy Content Sections */}
      <div className="space-y-10 text-sm text-[var(--text-secondary)] leading-relaxed font-body">
        {/* 1. Core Architecture */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">01.</span>
            In-Memory Document Ingestion
          </h2>
          <p>
            When you submit a resume (PDF, DOCX, or plain text) and an optional job description, text extraction is executed inside temporary RAM buffers using Apache PDFBox and Apache POI.
          </p>
          <ul className="space-y-2 text-xs sm:text-sm pl-4 list-disc marker:text-[var(--brand-500)]">
            <li>
              <strong className="text-[var(--text)]">Guest Scans:</strong> If you are not signed in, calculations run ephemerally. As soon as the HTTP analysis response completes, all in-memory buffers are deallocated.
            </li>
            <li>
              <strong className="text-[var(--text)]">Authenticated Users:</strong> Only the calculated numeric scores, keyword breakdown summaries, and evaluation timestamps are saved to your personal history. The raw binary PDF or DOCX file is never stored.
            </li>
          </ul>
        </section>

        {/* 2. Account Information */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">02.</span>
            Account and Authentication Data
          </h2>
          <p>
            Account registration is completely optional. For registered users:
          </p>
          <ul className="space-y-2 text-xs sm:text-sm pl-4 list-disc marker:text-[var(--brand-500)]">
            <li>
              <strong className="text-[var(--text)]">Email Address:</strong> Used solely to authenticate your account and associate your score history.
            </li>
            <li>
              <strong className="text-[var(--text)]">Passwords:</strong> Stored using salted BCrypt cryptographic hashes. We never have access to your plaintext password.
            </li>
            <li>
              <strong className="text-[var(--text)]">Session Management:</strong> Handled via signed JSON Web Tokens (JWT) kept in your browser local storage.
            </li>
          </ul>
        </section>

        {/* 3. Cookies and Local Storage */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">03.</span>
            Local Storage and Cookies
          </h2>
          <p>
            ResumeLens uses strictly necessary client-side storage keys to keep the application functional:
          </p>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border border-[var(--border)] rounded-[9px] overflow-hidden">
              <thead className="bg-[var(--bg-elevated)] text-[var(--text)] border-b border-[var(--border)] font-mono">
                <tr>
                  <th className="p-3">Key Name</th>
                  <th className="p-3">Storage Type</th>
                  <th className="p-3">Function</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--border)]">
                <tr>
                  <td className="p-3 font-mono text-[var(--text)]">resumelens_token</td>
                  <td className="p-3">Local Storage</td>
                  <td className="p-3">Stores active JWT for account sessions.</td>
                </tr>
                <tr>
                  <td className="p-3 font-mono text-[var(--text)]">resumelens_user_email</td>
                  <td className="p-3">Local Storage</td>
                  <td className="p-3">Stores the displayed profile email address.</td>
                </tr>
                <tr>
                  <td className="p-3 font-mono text-[var(--text)]">resumelens_cookie_consent_v1</td>
                  <td className="p-3">Local Storage</td>
                  <td className="p-3">Records your cookie preferences choice.</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p className="text-xs text-[var(--text-tertiary)] pt-1">
            You can modify your preferences at any time by clicking "Cookie Preferences" in the footer or clearing your browser cache.
          </p>
        </section>

        {/* 4. Data Rights and Deletion */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">04.</span>
            Your Rights and Account Deletion
          </h2>
          <p>
            You retain complete ownership over your career materials:
          </p>
          <ul className="space-y-2 text-xs sm:text-sm pl-4 list-disc marker:text-[var(--brand-500)]">
            <li>
              You can wipe specific reports from your history dashboard at any time.
            </li>
            <li>
              You can request full removal of your registered account and all associated metrics by contacting the maintainer directly.
            </li>
          </ul>
        </section>

        {/* 5. Contact & Open Source Auditing */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">05.</span>
            Open Source Transparency & Inquiries
          </h2>
          <p>
            Our data handling claims can be inspected in our open-source repository at{' '}
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
            For privacy inquiries or technical questions:
            <br />
            Maintainer: <span className="text-[var(--text)] font-medium">Chirag Dwivedi</span>
            <br />
            Email:{' '}
            <a
              href="mailto:chiragdwivediji@gmail.com"
              className="text-[var(--brand-500)] hover:underline font-medium"
            >
              chiragdwivediji@gmail.com
            </a>
          </p>
        </section>
      </div>
    </div>
  );
}
