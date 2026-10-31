import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Check, AlertCircle } from 'lucide-react';
import DropZone from '../components/DropZone';
import DocumentMockup from '../components/DocumentMockup';
import ScoreGauge from '../components/ScoreGauge';
import WeightsStrip from '../components/WeightsStrip';
import HeroBackground from '../components/HeroBackground';

const SAMPLE_REPORTS = [
  {
    id: 'backend',
    label: 'Backend Engineer',
    role: 'Senior Java / Spring Boot Engineer',
    score: 84,
    tier: 'Strong Match',
    matchedCount: 36,
    totalCount: 42,
    percent: 86,
    matchedKeywords: ['Java 17', 'Spring Boot', 'REST APIs', 'PostgreSQL', 'Docker', 'Git', 'Kafka', 'Redis'],
    missingKeywords: ['Kubernetes', 'AWS Lambda', 'GraphQL'],
    checks: [
      { text: 'Single column parse-safe layout', pass: true },
      { text: 'Standard contact header detected', pass: true },
      { text: 'Quantified metrics in 82% of bullet points', pass: true },
      { text: 'Missing 3 cloud orchestration keywords', pass: false },
    ],
  },
  {
    id: 'frontend',
    label: 'Frontend Developer',
    role: 'React / Frontend Engineer',
    score: 68,
    tier: 'Moderate Match',
    matchedCount: 26,
    totalCount: 38,
    percent: 68,
    matchedKeywords: ['React', 'JavaScript', 'TypeScript', 'Tailwind CSS', 'HTML5', 'Redux'],
    missingKeywords: ['Next.js', 'Web Vitals', 'CI/CD Pipeline', 'GraphQL', 'Jest'],
    checks: [
      { text: 'Visual formatting clean and legible', pass: true },
      { text: 'All text content successfully extracted', pass: true },
      { text: 'Job description keywords below 70%', pass: false },
      { text: 'Quantified metrics missing in experience section', pass: false },
    ],
  },
  {
    id: 'cloud',
    label: 'DevOps & Cloud',
    role: 'Cloud Infrastructure / DevOps Engineer',
    score: 92,
    tier: 'Excellent Match',
    matchedCount: 41,
    totalCount: 45,
    percent: 91,
    matchedKeywords: ['Kubernetes', 'Docker', 'Terraform', 'AWS', 'Linux', 'CI/CD', 'Prometheus', 'Bash'],
    missingKeywords: ['Ansible', 'Golang'],
    checks: [
      { text: 'Zero unreadable tables or columns', pass: true },
      { text: 'Chronological work experience verified', pass: true },
      { text: 'Keyword density optimal (3.4%)', pass: true },
      { text: 'Security & contact info complete', pass: true },
    ],
  },
];

const WORKFLOW_STEPS = [
  {
    step: 1,
    badge: 'Step 01 &bull; Ingestion',
    title: 'Client-Side Ingestion & Privacy Guard',
    tagline: 'Original file not kept; only extracted text',
    description:
      'Upload any PDF or DOCX resume. The original file is not kept, only the extracted text so the report link works. Guest data is deleted after the retention period.',
    highlights: [
      'Original file is not kept; guest data deleted after retention period',
      'High-fidelity Apache PDFBox & POI text extraction',
      'Instant validation for unreadable font encodings',
    ],
  },
  {
    step: 2,
    badge: 'Step 02 &bull; Cross-Analysis',
    title: 'Target Job Alignment & Vector Extraction',
    tagline: 'Typo-tolerant semantic and exact keyword match',
    description:
      'Paste the target job description. The engine builds an exact skill taxonomy and analyzes keyword density, required qualifications, and industry variations.',
    highlights: [
      'Tolerates capitalization and common spelling variations',
      'Calculates hard requirements vs preferred qualifications',
      'Detects keyword stuffing and density penalties',
    ],
  },
  {
    step: 3,
    badge: 'Step 03 &bull; Diagnostic Report',
    title: 'Explainable Score & 1-Click Fix Roadmap',
    tagline: 'Transparent breakdown across 6 scoring dimensions',
    description:
      'Inspect your overall score, explore specific deductions, review detected compliance risks, and generate targeted prompts to fix every weak bullet point.',
    highlights: [
      '100% transparent math (no arbitrary AI scoring numbers)',
      'Downloadable PDF diagnostic report for offline review',
      'One-click prompt generator for instant resume rewriting',
    ],
  },
];

export default function LandingPage() {
  const [activeSample, setActiveSample] = useState(SAMPLE_REPORTS[0]);
  const [activeStep, setActiveStep] = useState(0);
  const [bulletMode, setBulletMode] = useState('after');
  const workflowScrollRef = useRef(null);

  useEffect(() => {
    document.title = 'ResumeLens - ATS Resume Scanner';
  }, []);

  const scrollToWorkflowStep = (idx) => {
    setActiveStep(idx);
    if (workflowScrollRef.current) {
      const container = workflowScrollRef.current;
      const stepElements = container.querySelectorAll('.workflow-stage-card');
      if (stepElements[idx]) {
        stepElements[idx].scrollIntoView({
          behavior: 'smooth',
          block: 'nearest',
          inline: 'start',
        });
      }
    }
  };

  return (
    <div className="w-full">
      {/* 7.1 Hero Section: Full Viewport First Screen */}
      <div className="relative overflow-hidden w-full min-h-screen flex flex-col justify-center pt-24 sm:pt-28 pb-12 sm:pb-16">
        <HeroBackground />

        <section
          id="upload-section"
          className="relative z-10 w-full max-w-[1536px] mx-auto px-6 sm:px-10 lg:px-16 py-6 sm:py-10 flex-1 flex items-center"
          aria-label="Hero section"
        >
          <div className="w-full grid grid-cols-1 lg:grid-cols-[1.1fr_0.9fr] gap-12 lg:gap-16 xl:gap-20 items-center">
            {/* Left Column: Value Proposition */}
            <motion.div
              initial={{ opacity: 0, y: 18 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, ease: [0.25, 1, 0.5, 1] }}
              className="flex flex-col gap-6 sm:gap-7 pt-2 relative z-10"
            >
              {/* Pill badge */}
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#E8602E]/10 border border-[#E8602E]/30 w-fit">
                <span className="text-[#E8602E] text-xs font-semibold tracking-wide">
                  Explainable scoring
                </span>
              </div>

              {/* Main heading */}
              <h1 className="text-4xl sm:text-5xl md:text-6xl lg:text-[64px] font-bold text-white leading-[1.08] tracking-tight font-heading max-w-[620px]">
                See your resume the way an <br className="hidden sm:inline" />ATS does
              </h1>

              {/* Body paragraph */}
              <p className="text-base sm:text-lg text-[#b0b0b0] font-normal leading-relaxed max-w-[560px]">
                Upload your resume and a job description. Get a transparent score, the keywords you are missing and specific fixes in seconds.
              </p>

              {/* Three feature badges */}
              <div className="flex flex-wrap items-center gap-2.5 sm:gap-3 pt-2">
                <div className="px-3.5 sm:px-4 py-2 rounded-xl bg-white/[0.04] border border-white/10 text-xs sm:text-sm font-medium text-neutral-300">
                  Results in seconds
                </div>
                <div className="px-3.5 sm:px-4 py-2 rounded-xl bg-white/[0.04] border border-white/10 text-xs sm:text-sm font-medium text-neutral-300">
                  Original file not kept; guest data deleted after retention period
                </div>
                <div className="px-3.5 sm:px-4 py-2 rounded-xl bg-white/[0.04] border border-white/10 text-xs sm:text-sm font-medium text-neutral-300">
                  Every score explained
                </div>
              </div>
            </motion.div>

            {/* Right Column: Upload Zone Card */}
            <motion.div
              initial={{ opacity: 0, scale: 0.97 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.55, delay: 0.1, ease: [0.25, 1, 0.5, 1] }}
              className="w-full relative z-10"
            >
              <DropZone />
            </motion.div>
          </div>
        </section>
      </div>

      {/* 7.2 Interactive Sample Report Section */}
      <section
        className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24 border-t border-[var(--border)]"
        aria-labelledby="sample-report-heading"
      >
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-10">
          <div>
            <span className="text-xs font-mono uppercase tracking-[0.08em] text-[var(--brand-500)] font-semibold">
              Interactive Preview
            </span>
            <h2
              id="sample-report-heading"
              className="font-heading font-extrabold text-3xl sm:text-4xl text-[var(--text)] tracking-tight mt-1.5"
            >
              Sample report
            </h2>
            <p className="text-sm sm:text-base text-[var(--text-secondary)] mt-2 max-w-2xl">
              Switch between candidate profiles to inspect keyword match rates, formatting risks, and scoring details.
            </p>
          </div>

          {/* Interactive Role Selector Tabs */}
          <div className="flex items-center gap-2 p-1.5 rounded-[12px] bg-[var(--bg-elevated)] border border-[var(--border)] self-start md:self-auto overflow-x-auto hide-scrollbar">
            {SAMPLE_REPORTS.map((sample) => {
              const isActive = activeSample.id === sample.id;
              return (
                <button
                  key={sample.id}
                  type="button"
                  onClick={() => setActiveSample(sample)}
                  className={`relative px-4 py-2 rounded-[8px] text-xs sm:text-sm font-semibold transition-colors whitespace-nowrap cursor-pointer ${
                    isActive ? 'text-[var(--on-brand)]' : 'text-[var(--text-secondary)] hover:text-[var(--text)]'
                  }`}
                >
                  {isActive && (
                    <motion.div
                      layoutId="activeSampleTabIndicator"
                      className="absolute inset-0 bg-[var(--brand-500)] rounded-[8px] shadow-sm -z-0"
                      transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                    />
                  )}
                  <span className="relative z-10">{sample.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-12 gap-8 lg:gap-12 items-center">
          {/* Left: DocumentMockup component */}
          <div className="md:col-span-5 lg:col-span-5 xl:col-span-4 flex justify-center">
            <div className="w-full max-w-xs sm:max-w-sm">
              <DocumentMockup scanning={true} />
            </div>
          </div>

          {/* Right: Interactive Report Card */}
          <div className="md:col-span-7 lg:col-span-7 xl:col-span-8 rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 xl:p-10 shadow-[0_20px_40px_rgba(0,0,0,0.30)] min-h-[360px]">
            <AnimatePresence mode="wait">
              <motion.div
                key={activeSample.id}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.2 }}
                className="flex flex-col gap-6"
              >
                <div className="flex items-center justify-between border-b border-[var(--border)] pb-4">
                  <div className="space-y-0.5">
                    <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-tertiary)]">
                      Evaluation Profile
                    </span>
                    <p className="font-heading font-semibold text-sm text-[var(--text)]">
                      {activeSample.role}
                    </p>
                  </div>
                  <span className="text-[11px] font-mono uppercase tracking-[0.08em] px-2.5 py-1 rounded-[6px] bg-[var(--bg-elevated)] border border-[var(--border)] text-[var(--text-secondary)] shrink-0">
                    Sample Report
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-12 gap-6 items-center">
                  {/* ScoreGauge */}
                  <div className="sm:col-span-5 flex justify-center">
                    <ScoreGauge score={activeSample.score} tierLabel={activeSample.tier} size={150} />
                  </div>

                  {/* Check items */}
                  <div className="sm:col-span-7 space-y-3">
                    {activeSample.checks.map((chk, idx) => (
                      <div key={idx} className="flex items-center gap-2.5">
                        <div
                          className={`w-5 h-5 rounded-full flex items-center justify-center shrink-0 border ${
                            chk.pass
                              ? 'bg-[rgba(0,164,109,0.12)] border-[var(--success)] text-[var(--success)]'
                              : 'bg-[rgba(255,155,0,0.12)] border-[var(--accent-orange)] text-[var(--accent-orange)]'
                          }`}
                        >
                          {chk.pass ? (
                            <Check className="w-3 h-3 stroke-[2.5]" />
                          ) : (
                            <AlertCircle className="w-3 h-3" />
                          )}
                        </div>
                        <span className="text-xs sm:text-sm font-medium text-[var(--text)]">
                          {chk.text}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Keyword match progress bar */}
                <div className="pt-4 border-t border-[var(--border)] space-y-3">
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-[var(--text-secondary)] font-medium">
                      Matched {activeSample.matchedCount} of {activeSample.totalCount} keywords
                    </span>
                    <span className="font-mono text-[var(--brand-500)] font-semibold">
                      {activeSample.percent}%
                    </span>
                  </div>
                  <div className="h-2 w-full bg-[var(--bg-elevated)] rounded-full overflow-hidden border border-[var(--border)]">
                    <div
                      className="h-full bg-[var(--brand-500)] rounded-full transition-all duration-500 ease-out"
                      style={{ width: `${activeSample.percent}%` }}
                    />
                  </div>

                  {/* Sample matched skill chips */}
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {activeSample.matchedKeywords.map((kw, i) => (
                      <span
                        key={i}
                        className="text-[11px] font-mono px-2 py-0.5 rounded-[4px] bg-[rgba(0,164,109,0.10)] border border-[rgba(0,164,109,0.25)] text-[var(--success)]"
                      >
                        +{kw}
                      </span>
                    ))}
                    {activeSample.missingKeywords.map((kw, i) => (
                      <span
                        key={i}
                        className="text-[11px] font-mono px-2 py-0.5 rounded-[4px] bg-[rgba(255,155,0,0.10)] border border-[rgba(255,155,0,0.25)] text-[var(--accent-orange)]"
                      >
                        -{kw}
                      </span>
                    ))}
                  </div>
                </div>
              </motion.div>
            </AnimatePresence>
          </div>
        </div>
      </section>

      {/* 7.3 How Your Score Is Built Section */}
      <section className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24 border-t border-[var(--border)]">
        <WeightsStrip />
      </section>

      {/* 7.4 Engine Capabilities (Clean Bento Matrix without icon clutter) */}
      <section
        className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24 border-t border-[var(--border)]"
        aria-labelledby="capabilities-heading"
      >
        <div className="text-center mb-12 sm:mb-16">
          <span className="text-xs font-mono uppercase tracking-[0.08em] text-[var(--brand-500)] font-semibold">
            Features
          </span>
          <h2
            id="capabilities-heading"
            className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-[var(--text)] tracking-tight mt-2"
          >
            Engine Capabilities
          </h2>
          <p className="text-base sm:text-lg text-[var(--text-secondary)] mt-3 max-w-2xl mx-auto">
            Accurate, rule-based ATS evaluation calibrated against modern applicant tracking systems.
          </p>
        </div>

        {/* Bento Grid Layout (Clean minimal design, no extra icons) */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-8">
          {/* Bento Card 1 (Span 7 cols): Keyword Intelligence */}
          <div className="md:col-span-7 rounded-[22px] bg-[var(--bg-card)] border border-[var(--border)] p-7 sm:p-9 flex flex-col justify-between hover:border-[var(--border-strong)] transition-colors shadow-sm">
            <div className="space-y-3 mb-8">
              <span className="text-xs font-mono uppercase tracking-[0.08em] text-[var(--brand-500)] font-semibold">
                Keyword Matching
              </span>
              <h3 className="font-heading font-bold text-xl sm:text-2xl text-[var(--text)]">
                Typo-tolerant keyword extraction
              </h3>
              <p className="text-base text-[var(--text-secondary)] leading-relaxed max-w-xl">
                Matches exact keywords, spelling variations, and abbreviations while guarding against keyword stuffing penalties.
              </p>
            </div>

            <div className="rounded-[10px] bg-[var(--bg-page)] border border-[var(--border)] p-4 space-y-3 font-mono text-xs">
              <div className="flex items-center justify-between text-[11px] text-[var(--text-secondary)] border-b border-[var(--border)] pb-2">
                <span>Extracted Skill Vector</span>
                <span className="text-[var(--success)] font-semibold">94.2% Alignment</span>
              </div>
              <div className="flex flex-wrap gap-2">
                <span className="px-2.5 py-1 rounded-[6px] bg-[rgba(0,164,109,0.12)] border border-[var(--success)]/30 text-[var(--success)]">
                  React 18 &bull; 4 occurrences
                </span>
                <span className="px-2.5 py-1 rounded-[6px] bg-[rgba(0,164,109,0.12)] border border-[var(--success)]/30 text-[var(--success)]">
                  TypeScript &bull; 3 occurrences
                </span>
                <span className="px-2.5 py-1 rounded-[6px] bg-[rgba(0,164,109,0.12)] border border-[var(--success)]/30 text-[var(--success)]">
                  Spring Boot &bull; 2 occurrences
                </span>
                <span className="px-2.5 py-1 rounded-[6px] bg-[rgba(255,155,0,0.12)] border border-[var(--accent-orange)]/30 text-[var(--accent-orange)]">
                  Docker &bull; Missing in summary
                </span>
              </div>
            </div>
          </div>

          {/* Bento Card 2 (Span 5 cols): Compliance & Risk Radar */}
          <div className="md:col-span-5 rounded-[16px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 flex flex-col justify-between hover:border-[var(--border-strong)] transition-colors shadow-sm">
            <div className="space-y-2 mb-6">
              <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--danger)] font-semibold">
                Compliance & Format
              </span>
              <h3 className="font-heading font-bold text-lg text-[var(--text)]">
                Parsing risk detection
              </h3>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
                Flags structural traps that cause ATS parsers to misread or discard sections of your document.
              </p>
            </div>

            <div className="space-y-2 text-xs">
              <div className="flex items-center justify-between p-2.5 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)]">
                <span className="text-[var(--text)] font-medium">Multi-column layout</span>
                <span className="px-2 py-0.5 rounded-[4px] text-[10px] font-mono bg-[rgba(239,68,68,0.12)] text-[var(--danger)] border border-[var(--danger)]/30">
                  High Risk
                </span>
              </div>
              <div className="flex items-center justify-between p-2.5 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)]">
                <span className="text-[var(--text)] font-medium">Header / Footer text loss</span>
                <span className="px-2 py-0.5 rounded-[4px] text-[10px] font-mono bg-[rgba(255,155,0,0.12)] text-[var(--accent-orange)] border border-[var(--accent-orange)]/30">
                  Medium
                </span>
              </div>
              <div className="flex items-center justify-between p-2.5 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)]">
                <span className="text-[var(--text)] font-medium">Standard UTF-8 fonts</span>
                <span className="px-2 py-0.5 rounded-[4px] text-[10px] font-mono bg-[rgba(0,164,109,0.12)] text-[var(--success)] border border-[var(--success)]/30">
                  Passed
                </span>
              </div>
            </div>
          </div>

          {/* Bento Card 3 (Span 5 cols): Quantifiable Impact */}
          <div className="md:col-span-5 rounded-[16px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 flex flex-col justify-between hover:border-[var(--border-strong)] transition-colors shadow-sm">
            <div className="space-y-2 mb-6">
              <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--info)] font-semibold">
                Impact Analysis
              </span>
              <h3 className="font-heading font-bold text-lg text-[var(--text)]">
                Quantified metrics auditor
              </h3>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
                Evaluates work experience bullets for measurable outcomes (percentages, revenue, latency reductions).
              </p>
            </div>

            <div className="rounded-[10px] bg-[var(--bg-page)] border border-[var(--border)] p-4 space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="text-[var(--text-secondary)] font-medium">Metric-Backed Statements</span>
                <span className="font-mono text-[var(--info)] font-bold">8 of 10 Bullets</span>
              </div>
              <div className="h-2 w-full bg-[var(--bg-elevated)] rounded-full overflow-hidden border border-[var(--border)]">
                <div className="h-full bg-[var(--info)] rounded-full" style={{ width: '80%' }} />
              </div>
              <p className="text-[11px] text-[var(--text-tertiary)] pt-1">
                Passing standard: &ge; 70% of bullets containing concrete metrics.
              </p>
            </div>
          </div>

          {/* Bento Card 4 (Span 7 cols): Interactive Prompt Fix */}
          <div className="md:col-span-7 rounded-[16px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 flex flex-col justify-between hover:border-[var(--border-strong)] transition-colors shadow-sm">
            <div className="space-y-2 mb-6">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--brand-500)] font-semibold">
                  Actionable Rewriting
                </span>
                {/* Before / After toggle */}
                <div className="flex items-center gap-1 p-1 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)]">
                  <button
                    type="button"
                    onClick={() => setBulletMode('before')}
                    className={`px-2.5 py-1 rounded-[6px] text-[11px] font-semibold transition-colors cursor-pointer ${
                      bulletMode === 'before'
                        ? 'bg-[var(--bg-card)] text-[var(--text)]'
                        : 'text-[var(--text-secondary)]'
                    }`}
                  >
                    Before
                  </button>
                  <button
                    type="button"
                    onClick={() => setBulletMode('after')}
                    className={`px-2.5 py-1 rounded-[6px] text-[11px] font-semibold transition-colors cursor-pointer ${
                      bulletMode === 'after'
                        ? 'bg-[var(--brand-500)] text-[var(--on-brand)]'
                        : 'text-[var(--text-secondary)]'
                    }`}
                  >
                    ATS Optimized
                  </button>
                </div>
              </div>

              <h3 className="font-heading font-bold text-lg text-[var(--text)]">
                Specific fixes and prompts
              </h3>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
                Transform passive descriptions into strong action-verb bullets tailored for AI prompt assistants.
              </p>
            </div>

            <div className="p-4 rounded-[10px] bg-[var(--bg-page)] border border-[var(--border)] transition-all duration-200">
              {bulletMode === 'before' ? (
                <div className="space-y-1.5">
                  <span className="text-[10px] font-mono uppercase tracking-wider text-[var(--danger)] font-semibold">
                    &bull; Weak phrasing (Score 42)
                  </span>
                  <p className="text-xs text-[var(--text-secondary)] line-through">
                    &ldquo;Worked on frontend and backend features for web application and helped improve team processes.&rdquo;
                  </p>
                </div>
              ) : (
                <div className="space-y-1.5">
                  <span className="text-[10px] font-mono uppercase tracking-wider text-[var(--success)] font-semibold">
                    &bull; ATS High Impact (Score 96)
                  </span>
                  <p className="text-xs text-[var(--text)] font-medium">
                    &ldquo;Architected full-stack service in Spring Boot & React, reducing page load latency by 42% and processing 15k+ daily requests.&rdquo;
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* 7.5 How It Works (Interactive Scrollable / Tabbed Pipeline) */}
      <section
        className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24 border-t border-[var(--border)]"
        aria-labelledby="how-it-works-heading"
      >
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
          <div>
            <span className="text-xs font-mono uppercase tracking-[0.08em] text-[var(--brand-500)] font-semibold">
              Workflow
            </span>
            <h2
              id="how-it-works-heading"
              className="font-heading font-extrabold text-3xl sm:text-4xl text-[var(--text)] tracking-tight mt-1.5"
            >
              How it works
            </h2>
            <p className="text-base text-[var(--text-secondary)] mt-2 max-w-2xl">
              From raw intake to comprehensive ATS diagnosis in three automated steps.
            </p>
          </div>

          {/* Step selector pills */}
          <div className="flex items-center gap-2">
            {WORKFLOW_STEPS.map((s, idx) => (
              <button
                key={s.step}
                type="button"
                onClick={() => scrollToWorkflowStep(idx)}
                className={`flex items-center gap-2 px-3.5 py-1.5 rounded-[8px] text-xs font-semibold border transition-all cursor-pointer ${
                  activeStep === idx
                    ? 'bg-[var(--bg-elevated)] border-[var(--brand-500)] text-[var(--text)] shadow-sm'
                    : 'bg-[var(--bg-card)] border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)]'
                }`}
              >
                <span
                  className={`w-4 h-4 rounded-full flex items-center justify-center text-[10px] font-mono ${
                    activeStep === idx
                      ? 'bg-[var(--brand-500)] text-[var(--on-brand)]'
                      : 'bg-[var(--bg-elevated)] text-[var(--text-secondary)]'
                  }`}
                >
                  {s.step}
                </span>
                <span className="hidden sm:inline">Step {s.step}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Scrollable / Interactive Workflow Cards Container */}
        <div
          ref={workflowScrollRef}
          className="flex overflow-x-auto gap-6 pb-6 pt-2 snap-x snap-mandatory hide-scrollbar"
        >
          {WORKFLOW_STEPS.map((item, idx) => (
            <div
              key={item.step}
              className={`workflow-stage-card shrink-0 w-full md:w-[calc(100%-2rem)] lg:w-[calc(100%-4rem)] rounded-[18px] bg-[var(--bg-card)] border transition-all duration-300 p-6 sm:p-8 lg:p-10 snap-center shadow-lg ${
                activeStep === idx
                  ? 'border-[var(--brand-500)] shadow-[0_20px_50px_rgba(232,96,46,0.12)]'
                  : 'border-[var(--border)] hover:border-[var(--border-strong)]'
              }`}
            >
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
                {/* Left: Step Information */}
                <div className="lg:col-span-7 space-y-4">
                  <div className="flex items-center gap-3">
                    <span className="font-mono text-xs font-bold text-[var(--brand-500)] px-2.5 py-1 rounded-[6px] bg-[rgba(232,96,46,0.12)] border border-[rgba(232,96,46,0.25)]">
                      0{item.step}
                    </span>
                    <span className="text-xs font-mono uppercase tracking-wider text-[var(--text-secondary)]">
                      {item.tagline}
                    </span>
                  </div>

                  <h3 className="font-heading font-extrabold text-2xl sm:text-3xl text-[var(--text)] tracking-tight">
                    {item.title}
                  </h3>

                  <p className="text-sm sm:text-base text-[var(--text-secondary)] leading-relaxed">
                    {item.description}
                  </p>

                  <div className="space-y-2.5 pt-2">
                    {item.highlights.map((point, pIdx) => (
                      <div key={pIdx} className="flex items-center gap-2.5">
                        <div className="w-1.5 h-1.5 rounded-full bg-[var(--brand-500)] shrink-0" />
                        <span className="text-xs sm:text-sm font-medium text-[var(--text)]">
                          {point}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Right: Stage Visual */}
                <div className="lg:col-span-5 rounded-[12px] bg-[var(--bg-elevated)] border border-[var(--border)] p-6 space-y-4">
                  {item.step === 1 && (
                    <div className="space-y-4">
                      <div className="flex items-center justify-between pb-3 border-b border-[var(--border)]">
                        <span className="text-xs font-mono text-[var(--brand-500)] font-semibold">
                          Ingestion Status: Verified
                        </span>
                        <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-[rgba(0,164,109,0.12)] text-[var(--success)]">
                          Zero Disk Storage
                        </span>
                      </div>
                      <div className="space-y-2">
                        <div className="text-xs font-mono text-[var(--text)]">
                          resume_document.pdf (742 words extracted)
                        </div>
                        <div className="p-3 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)] font-mono text-[11px] text-[var(--text-secondary)] leading-relaxed">
                          &gt; Scanning section boundaries... [OK]<br />
                          &gt; Extracting experience timeline... [OK]<br />
                          &gt; Parsing skills dictionary... [OK]
                        </div>
                      </div>
                    </div>
                  )}

                  {item.step === 2 && (
                    <div className="space-y-4">
                      <div className="flex items-center justify-between pb-3 border-b border-[var(--border)]">
                        <span className="text-xs font-mono text-[var(--brand-500)] font-semibold">
                          Cross-Match Matrix
                        </span>
                        <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-[var(--bg-page)] text-[var(--text-secondary)]">
                          Target Spec
                        </span>
                      </div>
                      <div className="space-y-2">
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-[var(--text-secondary)]">Hard Technical Requirements</span>
                          <span className="font-mono text-[var(--success)] font-semibold">92% Match</span>
                        </div>
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-[var(--text-secondary)]">Seniority & Leadership Scope</span>
                          <span className="font-mono text-[var(--warning)] font-semibold">80% Match</span>
                        </div>
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-[var(--text-secondary)]">Domain Tools & Frameworks</span>
                          <span className="font-mono text-[var(--info)] font-semibold">88% Match</span>
                        </div>
                      </div>
                    </div>
                  )}

                  {item.step === 3 && (
                    <div className="space-y-4">
                      <div className="flex items-center justify-between pb-3 border-b border-[var(--border)]">
                        <span className="text-xs font-mono text-[var(--brand-500)] font-semibold">
                          Diagnostic Deliverables
                        </span>
                        <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-[rgba(0,164,109,0.12)] text-[var(--success)]">
                          Ready in &lt; 3s
                        </span>
                      </div>
                      <div className="grid grid-cols-2 gap-3 text-center">
                        <div className="p-3 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)]">
                          <span className="block font-heading font-extrabold text-xl text-[var(--brand-500)]">
                            84/100
                          </span>
                          <span className="text-[11px] text-[var(--text-secondary)]">Overall Score</span>
                        </div>
                        <div className="p-3 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)]">
                          <span className="block font-heading font-extrabold text-xl text-[var(--success)]">
                            PDF Ready
                          </span>
                          <span className="text-[11px] text-[var(--text-secondary)]">Export Report</span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 7.6 Bottom CTA Section */}
      <section className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-28 border-t border-[var(--border)] text-center">
        <div className="rounded-[28px] bg-[var(--bg-card)] border border-[var(--border)] p-8 sm:p-14 lg:p-20 relative overflow-hidden shadow-2xl">
          <div className="relative max-w-3xl mx-auto space-y-8">
            <h2 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-[var(--text)] tracking-tight">
              Test your resume against applicant tracking systems today
            </h2>
            <p className="text-base sm:text-lg text-[var(--text-secondary)] leading-relaxed">
              Upload your resume above or scroll up to drop your file. Get your free explainable score in seconds.
            </p>
            <div className="pt-3">
              <button
                type="button"
                onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
                className="inline-flex items-center justify-center px-10 py-4 rounded-full bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] font-semibold text-base transition-all shadow-[var(--shadow-primary-hover)] cursor-pointer"
              >
                Upload resume now
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
