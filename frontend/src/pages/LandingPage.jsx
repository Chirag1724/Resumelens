import { useEffect } from 'react';
import { Sparkles, Check, Zap, Search, Wrench } from 'lucide-react';
import DropZone from '../components/DropZone';
import DocumentMockup from '../components/DocumentMockup';
import ScoreGauge from '../components/ScoreGauge';
import WeightsStrip from '../components/WeightsStrip';
import FeatureCard from '../components/FeatureCard';
import StepCard from '../components/StepCard';

/**
 * LandingPage component
 * Assembles the full landing and upload page at / in accordance with docs/FRONTEND_DESIGN.md.
 */
export default function LandingPage() {
  useEffect(() => {
    document.title = 'ResumeLens - ATS Resume Scanner';
  }, []);

  return (
    <div className="w-full">
      {/* 7.1 Hero Section (Two Columns) */}
      <section
        className="max-w-6xl mx-auto px-4 sm:px-6 pt-10 sm:pt-16 pb-12 lg:pb-20"
        aria-label="Hero section"
      >
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center">
          {/* Left Column: Value Proposition */}
          <div className="lg:col-span-6 flex flex-col items-start">
            {/* Pill badge with spark icon */}
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[var(--bg-card)] border border-[var(--border)] text-xs font-semibold text-[var(--brand-500)] mb-6 shadow-sm">
              <Sparkles className="w-3.5 h-3.5" aria-hidden="true" />
              <span>Explainable scoring</span>
            </div>

            {/* Main heading */}
            <h1 className="font-heading font-extrabold text-[var(--text)] tracking-tight text-4xl sm:text-5xl lg:text-[3.75rem] leading-[1.05] mb-6">
              See your resume the way an ATS does
            </h1>

            {/* Body paragraph */}
            <p className="font-body text-base sm:text-lg text-[var(--text-secondary)] leading-relaxed mb-8 max-w-xl">
              Upload your resume and a job description. Get a transparent score, the keywords you are missing and specific fixes in seconds.
            </p>

            {/* Three small badge elements */}
            <div className="flex flex-wrap gap-2.5 sm:gap-3">
              <div className="px-3 py-1.5 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] text-xs font-medium text-[var(--text)]">
                Results in seconds
              </div>
              <div className="px-3 py-1.5 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] text-xs font-medium text-[var(--text)]">
                File is not stored, only extracted text
              </div>
              <div className="px-3 py-1.5 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] text-xs font-medium text-[var(--text)]">
                Every score explained
              </div>
            </div>
          </div>

          {/* Right Column: Upload Zone Card */}
          <div className="lg:col-span-6 w-full">
            <DropZone />
          </div>
        </div>
      </section>

      {/* 7.2 Sample Report Section */}
      <section
        className="max-w-5xl mx-auto px-4 sm:px-6 py-12 lg:py-16 border-t border-[var(--border)]"
        aria-labelledby="sample-report-heading"
      >
        <div className="text-center mb-10">
          <h2
            id="sample-report-heading"
            className="font-heading font-bold text-2xl sm:text-3xl text-[var(--text)] tracking-tight"
          >
            Sample report
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-12 gap-8 items-center">
          {/* Left: DocumentMockup component */}
          <div className="md:col-span-5 flex justify-center">
            <div className="w-full max-w-xs">
              <DocumentMockup scanning={false} />
            </div>
          </div>

          {/* Right: Card with label 'Sample report, not your data' */}
          <div className="md:col-span-7 rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 flex flex-col gap-6">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-mono uppercase tracking-[0.08em] px-2.5 py-1 rounded-[6px] bg-[var(--bg-elevated)] border border-[var(--border)] text-[var(--text-secondary)]">
                Sample report, not your data
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-12 gap-6 items-center">
              {/* ScoreGauge showing 77 and Strong Match */}
              <div className="sm:col-span-5 flex justify-center">
                <ScoreGauge score={77} tierLabel="Strong Match" size={150} />
              </div>

              {/* Four check items */}
              <div className="sm:col-span-7 space-y-3">
                <div className="flex items-center gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-[rgba(0,164,109,0.12)] border border-[var(--success)] flex items-center justify-center text-[var(--success)] shrink-0">
                    <Check className="w-3 h-3 stroke-[2.5]" />
                  </div>
                  <span className="text-xs sm:text-sm font-medium text-[var(--text)]">
                    Clear section order
                  </span>
                </div>

                <div className="flex items-center gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-[rgba(0,164,109,0.12)] border border-[var(--success)] flex items-center justify-center text-[var(--success)] shrink-0">
                    <Check className="w-3 h-3 stroke-[2.5]" />
                  </div>
                  <span className="text-xs sm:text-sm font-medium text-[var(--text)]">
                    No critical errors
                  </span>
                </div>

                <div className="flex items-center gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-[rgba(0,164,109,0.12)] border border-[var(--success)] flex items-center justify-center text-[var(--success)] shrink-0">
                    <Check className="w-3 h-3 stroke-[2.5]" />
                  </div>
                  <span className="text-xs sm:text-sm font-medium text-[var(--text)]">
                    Readable by parsers
                  </span>
                </div>

                <div className="flex items-center gap-2.5">
                  <div className="w-5 h-5 rounded-full bg-[rgba(0,164,109,0.12)] border border-[var(--success)] flex items-center justify-center text-[var(--success)] shrink-0">
                    <Check className="w-3 h-3 stroke-[2.5]" />
                  </div>
                  <span className="text-xs sm:text-sm font-medium text-[var(--text)]">
                    Keywords aligned
                  </span>
                </div>
              </div>
            </div>

            {/* Keyword match bar filled at 85 percent */}
            <div className="pt-4 border-t border-[var(--border)]">
              <div className="flex items-center justify-between text-xs mb-2">
                <span className="text-[var(--text-secondary)] font-medium">
                  Matched 34 of 40 keywords
                </span>
                <span className="font-mono text-[var(--brand-500)] font-semibold">
                  85%
                </span>
              </div>
              <div className="h-2 w-full bg-[var(--bg-elevated)] rounded-full overflow-hidden border border-[var(--border)]">
                <div
                  className="h-full bg-[var(--brand-500)] rounded-full transition-all duration-300"
                  style={{ width: '85%' }}
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 7.3 How Your Score Is Built Section */}
      <section className="max-w-5xl mx-auto px-4 sm:px-6 py-12 lg:py-16 border-t border-[var(--border)]">
        <WeightsStrip />
      </section>

      {/* 7.4 Features Section */}
      <section
        className="max-w-5xl mx-auto px-4 sm:px-6 py-12 lg:py-16 border-t border-[var(--border)]"
        aria-labelledby="features-heading"
      >
        <div className="text-center mb-10">
          <h2
            id="features-heading"
            className="font-heading font-bold text-2xl sm:text-3xl text-[var(--text)] tracking-tight"
          >
            Features
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          <FeatureCard
            icon={<Zap className="w-5 h-5" />}
            title="Instant analysis"
            description="Scoring runs in seconds, with a clear breakdown by category."
          />
          <FeatureCard
            icon={<Search className="w-5 h-5" />}
            title="Typo tolerant keywords"
            description="Spelling and formatting variations still count as a match."
          />
          <FeatureCard
            icon={<Wrench className="w-5 h-5" />}
            title="Specific fixes"
            description="Every risk comes with a fix, and every suggestion with an example."
          />
        </div>
      </section>

      {/* 7.5 How It Works Section */}
      <section
        className="max-w-5xl mx-auto px-4 sm:px-6 py-12 lg:py-16 border-t border-[var(--border)]"
        aria-labelledby="how-it-works-heading"
      >
        <div className="text-center mb-10">
          <h2
            id="how-it-works-heading"
            className="font-heading font-bold text-2xl sm:text-3xl text-[var(--text)] tracking-tight"
          >
            How it works
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          <StepCard
            stepNumber={1}
            title="Upload your resume"
            description="Choose a PDF or DOCX file to extract clean text without storing files."
          />
          <StepCard
            stepNumber={2}
            title="Add the job description"
            description="Paste the requirements to compare keyword coverage and role alignment."
          />
          <StepCard
            stepNumber={3}
            title="Review your report"
            description="Inspect your overall score, category breakdown, compliance risks, and concrete fixes."
          />
        </div>
      </section>

      {/* 7.6 Footer */}
      <footer className="w-full border-t border-[var(--border)] py-8 px-4 sm:px-6 text-center">
        <p className="text-xs text-[var(--text-tertiary)] max-w-2xl mx-auto leading-relaxed">
          ResumeLens. Scores are rule based estimates and do not guarantee any ATS result.
        </p>
      </footer>
    </div>
  );
}
