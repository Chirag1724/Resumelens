import { useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getScoreDetails } from '../lib/score';
import { useAuth } from '../context/AuthContext';
import { Upload, FileText, AlertTriangle, ArrowLeft } from 'lucide-react';

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function UploadPage() {
  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold font-heading text-[var(--text)]">
          Resume Intake and Job Description
        </h1>
        <p className="text-sm text-[var(--text-secondary)] mt-1">
          Upload your resume in PDF or DOCX format and paste target job description text for instant ATS scoring.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
        <div className="md:col-span-5 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6">
          <h2 className="text-base font-semibold font-heading text-[var(--text)] mb-4 flex items-center gap-2">
            <Upload className="w-4 h-4 text-[var(--brand-500)]" />
            <span>Resume Document (PDF/DOCX)</span>
          </h2>
          <div className="border-2 border-dashed border-[var(--border)] rounded-[30px] p-8 text-center bg-[var(--bg-page)]">
            <Upload className="w-8 h-8 text-[var(--text-tertiary)] mx-auto mb-3" />
            <p className="text-sm text-[var(--text-secondary)] mb-1">Drag and drop resume here</p>
            <p className="text-xs text-[var(--text-tertiary)]">Supports PDF and DOCX up to 10MB</p>
          </div>
          <div className="mt-4 text-xs text-[var(--text-tertiary)]">
            Guest uploads are supported without requiring an account.
          </div>
        </div>

        <div className="md:col-span-7 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6">
          <h2 className="text-base font-semibold font-heading text-[var(--text)] mb-4 flex items-center gap-2">
            <FileText className="w-4 h-4 text-[var(--brand-500)]" />
            <span>Target Job Description</span>
          </h2>
          <textarea
            rows={8}
            disabled
            placeholder="Paste raw job description text here..."
            className="w-full p-3 bg-[var(--bg-page)] border border-[var(--border)] rounded-[9px] text-sm text-[var(--text)] placeholder-[var(--text-tertiary)] resize-none"
          />
          <div className="mt-4 flex justify-between items-center">
            <span className="text-xs text-[var(--text-tertiary)]">0 words / 0 characters</span>
            <button
              type="button"
              disabled
              className="py-2 px-4 bg-[var(--brand-500)] text-[var(--on-brand)] text-sm font-semibold rounded-[9px] opacity-60 cursor-not-allowed"
            >
              Analyze Resume and Predict Score
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export function ReportPage() {
  const { id } = useParams();
  const isValid = id && UUID_REGEX.test(id);

  if (!isValid) {
    return <NotFoundPage message="The requested report identifier is not a valid UUID format." />;
  }

  const sampleScore = 78.45;
  const scoreInfo = getScoreDetails(sampleScore);

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 mb-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
                Match Result
              </span>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded-[6px] bg-[var(--bg-elevated)] border border-[var(--border)] text-[var(--text-secondary)]">
                {id}
              </span>
            </div>
            <h1 className="text-xl font-bold font-heading text-[var(--text)]">
              Analysis and Score Breakdown
            </h1>
          </div>
          <div className="flex items-center gap-3">
            <div
              className="px-3 py-1.5 rounded-[12px] text-xs font-semibold"
              style={{
                backgroundColor: `${scoreInfo.color}1F`,
                color: scoreInfo.color,
                border: `1px solid ${scoreInfo.color}40`,
              }}
            >
              {scoreInfo.label} ({sampleScore.toFixed(2)})
            </div>
          </div>
        </div>
      </div>

      <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 text-sm text-[var(--text-secondary)]">
        <p className="mb-2">
          Report placeholder for analysis UUID: <code className="text-[var(--text)] font-mono">{id}</code>
        </p>
        <p className="text-xs text-[var(--text-tertiary)]">
          Possession of this unguessable UUID grants read access under the public guest model.
        </p>
      </div>
    </div>
  );
}

export function HistoryPage() {
  const { userEmail } = useAuth();
  const sampleScore1 = 82.0;
  const sampleScore2 = 64.5;
  const score1 = getScoreDetails(sampleScore1);
  const score2 = getScoreDetails(sampleScore2);

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold font-heading text-[var(--text)]">
          Analysis History
        </h1>
        <p className="text-sm text-[var(--text-secondary)] mt-1">
          Historical resume evaluations saved for account: <span className="text-[var(--text)]">{userEmail}</span>
        </p>
      </div>

      <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6">
        <h2 className="text-[11px] font-semibold font-heading text-[var(--text-secondary)] uppercase tracking-[0.08em] mb-4">
          Recent Analyses (Placeholder)
        </h2>
        <div className="space-y-3">
          <div className="flex items-center justify-between p-4 bg-[var(--bg-page)] border border-[var(--border)] rounded-[12px]">
            <div>
              <p className="text-sm font-medium text-[var(--text)]">Senior Java Developer</p>
              <p className="text-xs text-[var(--text-tertiary)]">jane_doe_resume.pdf</p>
            </div>
            <div
              className="px-2.5 py-1 rounded-[6px] text-xs font-semibold"
              style={{
                backgroundColor: `${score1.color}1F`,
                color: score1.color,
                border: `1px solid ${score1.color}40`,
              }}
            >
              {score1.label} ({sampleScore1.toFixed(1)})
            </div>
          </div>

          <div className="flex items-center justify-between p-4 bg-[var(--bg-page)] border border-[var(--border)] rounded-[12px]">
            <div>
              <p className="text-sm font-medium text-[var(--text)]">Cloud Infrastructure Lead</p>
              <p className="text-xs text-[var(--text-tertiary)]">jane_doe_resume.pdf</p>
            </div>
            <div
              className="px-2.5 py-1 rounded-[6px] text-xs font-semibold"
              style={{
                backgroundColor: `${score2.color}1F`,
                color: score2.color,
                border: `1px solid ${score2.color}40`,
              }}
            >
              {score2.label} ({sampleScore2.toFixed(1)})
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export function NotFoundPage({ message }) {
  useEffect(() => {
    document.title = 'Page Not Found - ResumeLens';
  }, []);

  return (
    <div className="flex flex-col items-center justify-center min-h-[calc(100vh-8rem)] px-4 text-center">
      <div className="w-12 h-12 rounded-full bg-[rgba(239,68,68,0.12)] border border-[var(--danger)] flex items-center justify-center mb-4 text-[var(--danger)]">
        <AlertTriangle className="w-6 h-6" />
      </div>
      <h1 className="text-2xl font-bold font-heading text-[var(--text)] mb-2">
        Page Not Found
      </h1>
      <p className="text-sm text-[var(--text-secondary)] max-w-md mb-6">
        {message || 'The page you are looking for does not exist or has been moved.'}
      </p>
      <Link
        to="/"
        className="inline-flex items-center gap-2 py-2 px-4 bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-sm font-medium rounded-[9px] transition-colors"
      >
        <ArrowLeft className="w-4 h-4 text-[var(--text-secondary)]" />
        <span>Return to Home</span>
      </Link>
    </div>
  );
}
