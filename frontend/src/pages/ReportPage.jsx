import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  FileCheck,
  Cpu,
  TrendingUp,
  Layers,
  PenTool,
  ShieldCheck,
  Download,
  Sparkles,
  Copy,
  Check,
  AlertTriangle,
  ArrowLeft,
  RotateCcw,
  CheckCircle2,
  AlertCircle,
  FileText,
} from 'lucide-react';
import { getAnalysis, downloadReport } from '../api/client';
import { getScoreDetails } from '../lib/score';
import { SCORING_CATEGORIES } from '../lib/scoring';
import ScoreGauge from '../components/ScoreGauge';
import InfoCard from '../components/InfoCard';
import InsightCard from '../components/InsightCard';
import SummaryCard from '../components/SummaryCard';
import SectionScoreBar from '../components/SectionScoreBar';
import SectionDetailCard from '../components/SectionDetailCard';
import KeywordPill from '../components/KeywordPill';
import RiskCard from '../components/RiskCard';
import SuggestionCard from '../components/SuggestionCard';
import FixPromptModal from '../components/FixPromptModal';
import CtaCard from '../components/CtaCard';

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const DIMENSION_ICONS = {
  ATS_COMPATIBILITY: FileCheck,
  SKILLS_VALIDATION: Cpu,
  IMPACT_ACHIEVEMENTS: TrendingUp,
  PROJECT_QUALITY: Layers,
  WRITING_QUALITY: PenTool,
  PROFESSIONALISM: ShieldCheck,
};

function parseKeywords(raw) {
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed;
    } catch {
      return raw.split(',').map((s) => s.trim()).filter(Boolean);
    }
  }
  return [];
}

export default function ReportPage() {
  const { id } = useParams();
  const isValidUuid = Boolean(id && UUID_REGEX.test(id));

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [copySuccess, setCopySuccess] = useState(false);

  const fetchReport = useCallback(() => {
    if (!isValidUuid) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    getAnalysis(id)
      .then((res) => {
        setData(res);
        setLoading(false);
      })
      .catch((err) => {
        setError(err);
        setLoading(false);
      });
  }, [id, isValidUuid]);

  useEffect(() => {
    fetchReport();
  }, [fetchReport]);

  useEffect(() => {
    if (data?.fileName) {
      document.title = `${data.fileName} - Analysis Report - ResumeLens`;
    } else {
      document.title = 'Analysis Report - ResumeLens';
    }
  }, [data]);

  const handleDownloadPdf = async () => {
    if (!id || isDownloading) return;
    setIsDownloading(true);
    try {
      const blob = await downloadReport(id);
      const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'ats-report.pdf');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      // PDF download error handled quietly without altering screen
    } finally {
      setIsDownloading(false);
    }
  };

  const handleCopyLink = async () => {
    const currentUrl = window.location.href;
    let copied = false;
    if (navigator.clipboard && window.isSecureContext) {
      try {
        await navigator.clipboard.writeText(currentUrl);
        copied = true;
      } catch {
        copied = false;
      }
    }
    if (!copied) {
      try {
        const textArea = document.createElement('textarea');
        textArea.value = currentUrl;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        copied = document.execCommand('copy');
        textArea.remove();
      } catch {
        copied = false;
      }
    }
    if (copied) {
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    }
  };

  // Invalid UUID format shows 404 not found
  if (!isValidUuid) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[calc(100vh-8rem)] px-4 text-center">
        <div className="w-12 h-12 rounded-full bg-[rgba(239,68,68,0.12)] border border-[var(--danger)] flex items-center justify-center mb-4 text-[var(--danger)]">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <h1 className="text-2xl font-bold font-heading text-[var(--text)] mb-2">
          Report Not Found
        </h1>
        <p className="text-sm text-[var(--text-secondary)] max-w-md mb-6">
          The requested report identifier is not a valid capability UUID format.
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 py-2 px-4 bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text)] text-sm font-medium rounded-[9px] transition-colors"
        >
          <ArrowLeft className="w-4 h-4 text-[var(--text-secondary)]" />
          <span>Return to Home</span>
        </Link>
      </div>
    );
  }

  // Loading skeleton state
  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8 animate-pulse space-y-8">
        {/* Action bar skeleton */}
        <div className="h-14 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px]" />

        {/* Top row skeleton */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
          <div className="md:col-span-4 h-64 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px]" />
          <div className="md:col-span-8 h-64 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px]" />
        </div>

        {/* Quick insights skeleton */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="h-28 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px]" />
          ))}
        </div>

        {/* Summary card skeleton */}
        <div className="h-24 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px]" />

        {/* Section bars skeleton */}
        <div className="space-y-3">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="h-20 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px]" />
          ))}
        </div>
      </div>
    );
  }

  // 404 not found error state
  if (error && error.status === 404) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[calc(100vh-8rem)] px-4 text-center">
        <div className="w-12 h-12 rounded-full bg-[rgba(239,68,68,0.12)] border border-[var(--danger)] flex items-center justify-center mb-4 text-[var(--danger)]">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <h1 className="text-2xl font-bold font-heading text-[var(--text)] mb-2">
          Report Not Found
        </h1>
        <p className="text-sm text-[var(--text-secondary)] max-w-md mb-6">
          No analysis result was found for this report ID. It may have expired or been removed.
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 py-2 px-4 bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text)] text-sm font-medium rounded-[9px] transition-colors"
        >
          <ArrowLeft className="w-4 h-4 text-[var(--text-secondary)]" />
          <span>Return to Home</span>
        </Link>
      </div>
    );
  }

  // Other error state with retry button
  if (error || !data) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[calc(100vh-8rem)] px-4 text-center">
        <div className="w-12 h-12 rounded-full bg-[rgba(239,68,68,0.12)] border border-[var(--danger)] flex items-center justify-center mb-4 text-[var(--danger)]">
          <AlertCircle className="w-6 h-6" />
        </div>
        <h1 className="text-2xl font-bold font-heading text-[var(--text)] mb-2">
          Unable to Load Report
        </h1>
        <p className="text-sm text-[var(--text-secondary)] max-w-md mb-6">
          {error?.message || 'An unexpected error occurred while fetching the analysis report.'}
        </p>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={fetchReport}
            className="inline-flex items-center gap-2 py-2 px-4 bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-sm font-semibold rounded-[9px] transition-colors cursor-pointer"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Retry</span>
          </button>
          <Link
            to="/"
            className="inline-flex items-center gap-2 py-2 px-4 bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text)] text-sm font-medium rounded-[9px] transition-colors"
          >
            <span>Return to Home</span>
          </Link>
        </div>
      </div>
    );
  }

  // Derive data points strictly as specified
  const overallScore = Math.max(0, Math.min(100, Math.round(Number(data.overallScore) || 0)));
  const overallScoreInfo = getScoreDetails(data.statusLabel || overallScore);
  const tierLabel = data.statusLabel || overallScoreInfo.label;

  const rawSectionScores = Array.isArray(data.sectionScores) ? data.sectionScores : [];

  // Match SCORING_CATEGORIES with API sectionScores
  const categoriesMap = new Map(SCORING_CATEGORIES.map((c) => [c.sectionName, c]));

  const enrichedSections = SCORING_CATEGORIES.map((cat) => {
    const apiSection = rawSectionScores.find((s) => s.sectionName === cat.sectionName);
    const scoreVal = apiSection ? Number(apiSection.score) : 0;
    const scoreDetails = getScoreDetails(apiSection?.status || scoreVal);
    return {
      sectionName: cat.sectionName,
      name: cat.name,
      weight: cat.weight,
      description: cat.description,
      score: scoreVal,
      status: apiSection?.status || scoreDetails.tier,
      feedback: apiSection?.feedback || '',
      matchedKeywords: parseKeywords(apiSection?.matchedKeywords),
      missingKeywords: parseKeywords(apiSection?.missingKeywords),
      icon: DIMENSION_ICONS[cat.sectionName] || FileText,
    };
  });

  // Skills validation section data
  const skillsCategory = enrichedSections.find((s) => s.sectionName === 'SKILLS_VALIDATION');
  const matchedKeywords = skillsCategory?.matchedKeywords || [];
  const missingKeywords = skillsCategory?.missingKeywords || [];
  const totalKeywords = matchedKeywords.length + missingKeywords.length;

  // Risks list and counts
  const risks = Array.isArray(data.risks) ? data.risks : [];
  const highRisks = risks.filter((r) => String(r.severity).toUpperCase() === 'HIGH');
  const mediumRisks = risks.filter((r) => String(r.severity).toUpperCase() === 'MEDIUM');
  const highRiskCount = highRisks.length;
  const mediumRiskCount = mediumRisks.length;

  // Suggestions list
  const suggestions = Array.isArray(data.suggestions) ? data.suggestions : [];

  // Strengths and Improvements
  // Strengths: Section categories with status STRONG or EXCELLENT
  // Improvements: Section categories with status WEAK or POOR
  const strengths = enrichedSections.filter((s) => {
    const st = String(s.status).toUpperCase();
    return st === 'STRONG' || st === 'EXCELLENT' || s.score >= 70;
  });
  const improvements = enrichedSections.filter((s) => {
    const st = String(s.status).toUpperCase();
    return st === 'WEAK' || st === 'POOR' || s.score < 55;
  });

  // Weakest scoring section for Quick Insights
  const weakestSection = enrichedSections.reduce((worst, curr) =>
    curr.score < worst.score ? curr : worst,
    enrichedSections[0] || { name: 'ATS Compatibility', score: 0 }
  );

  return (
    <div className="max-w-5xl mx-auto px-4 py-8 space-y-8">
      {/* 8.10 Action Buttons Row */}
      <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
            Report capability ID:
          </span>
          <span className="text-[11px] font-mono px-2 py-0.5 rounded-[6px] bg-[var(--bg-elevated)] border border-[var(--border)] text-[var(--text-secondary)] truncate max-w-[200px] sm:max-w-xs">
            {id}
          </span>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Download PDF report button */}
          <button
            type="button"
            onClick={handleDownloadPdf}
            disabled={isDownloading}
            className="inline-flex items-center gap-2 px-3.5 py-2 rounded-[9px] bg-[var(--bg-elevated)] hover:bg-[var(--bg-page)] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-xs font-semibold transition-colors disabled:opacity-50 cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
          >
            <Download className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
            <span>{isDownloading ? 'Downloading...' : 'Download PDF report'}</span>
          </button>

          {/* Generate fix prompt button */}
          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="inline-flex items-center gap-2 px-3.5 py-2 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-xs font-semibold transition-colors shadow-[var(--shadow-primary-hover)] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
          >
            <Sparkles className="w-4 h-4" aria-hidden="true" />
            <span>Generate fix prompt</span>
          </button>

          {/* Copy link button with helper text */}
          <div className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-2">
            <button
              type="button"
              onClick={handleCopyLink}
              className="inline-flex items-center gap-2 px-3.5 py-2 rounded-[9px] bg-[var(--bg-elevated)] hover:bg-[var(--bg-page)] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-xs font-semibold transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
            >
              {copySuccess ? (
                <>
                  <Check className="w-4 h-4 text-[var(--success)]" aria-hidden="true" />
                  <span>Copied</span>
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4 text-[var(--text-secondary)]" aria-hidden="true" />
                  <span>Copy link</span>
                </>
              )}
            </button>
            <span className="text-[11px] text-[var(--text-tertiary)] italic">
              anyone with the link can view this report
            </span>
          </div>
        </div>
      </div>

      {/* 8.1 Top Row */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-stretch">
        {/* ScoreGauge Card */}
        <div className="md:col-span-5 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 flex flex-col items-center justify-center text-center">
          <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)] mb-4">
            Overall ATS Score
          </span>
          <ScoreGauge
            score={overallScore}
            tierLabel={tierLabel}
            size={180}
            showLabel={true}
          />
        </div>

        {/* File Info Card */}
        <div className="md:col-span-7 flex flex-col">
          <InfoCard
            fileName={data.fileName}
            analyzedAt={data.analyzedAt}
            targetRole={data.targetRole}
            experienceYears={data.experienceYears}
          />
        </div>
      </div>

      {/* 8.2 Quick Insights Row (Four Cards) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: Overall score tier label */}
        <InsightCard
          title="Overall Tier"
          value={tierLabel}
          subtext={`Score ${overallScore} out of 100`}
          badgeText={overallScoreInfo.tier}
          badgeColor={overallScoreInfo.color}
          icon={CheckCircle2}
        />

        {/* Card 2: Keywords matched vs total extracted */}
        <InsightCard
          title="Keywords Matched"
          value={totalKeywords > 0 ? `${matchedKeywords.length} of ${totalKeywords}` : `${Math.round(data.keywordMatchPercent || 0)}%`}
          subtext={
            missingKeywords.length === 1
              ? '1 missing keyword'
              : `${missingKeywords.length} missing keywords`
          }
          badgeText={
            totalKeywords > 0
              ? `${Math.round((matchedKeywords.length / totalKeywords) * 100)}%`
              : `${Math.round(data.keywordMatchPercent || 0)}%`
          }
          badgeColor="var(--success)"
          icon={Cpu}
        />

        {/* Card 3: High and medium risk counts */}
        <InsightCard
          title="Compliance Risks"
          value={highRiskCount + mediumRiskCount}
          subtext={`${highRiskCount} high, ${mediumRiskCount} medium severity`}
          badgeText={highRiskCount > 0 ? `${highRiskCount} High` : 'Clear'}
          badgeColor={highRiskCount > 0 ? 'var(--danger)' : 'var(--success)'}
          icon={AlertTriangle}
        />

        {/* Card 4: Weakest scoring section */}
        <InsightCard
          title="Weakest Dimension"
          value={weakestSection.name}
          subtext={`Score: ${Math.round(weakestSection.score)} out of 100`}
          badgeText={`${Math.round(weakestSection.score)}/100`}
          badgeColor={getScoreDetails(weakestSection.score).color}
          icon={TrendingUp}
        />
      </div>

      {/* 8.3 Summary Card */}
      <SummaryCard
        overallScore={overallScore}
        tierLabel={tierLabel}
        sectionScores={enrichedSections}
        highRiskCount={highRiskCount}
        missingKeywordCount={missingKeywords.length}
      />

      {/* 8.4 Strengths and Improvements */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Strengths Panel */}
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-2.5 h-2.5 rounded-full bg-[var(--success)]" />
            <h2 className="font-heading font-semibold text-sm text-[var(--text)] uppercase tracking-wider">
              Strengths
            </h2>
          </div>
          {strengths.length > 0 ? (
            <div className="space-y-3">
              {strengths.map((item) => {
                const itemScoreInfo = getScoreDetails(item.score);
                return (
                  <div
                    key={item.sectionName}
                    className="flex items-center justify-between p-3 rounded-[9px] bg-[var(--bg-page)] border border-[var(--border)]"
                  >
                    <span className="text-xs font-medium text-[var(--text)]">
                      {item.name}
                    </span>
                    <div className="flex items-center gap-2">
                      <span className="font-heading font-bold text-xs text-[var(--text)]">
                        {Math.round(item.score)}/100
                      </span>
                      <span
                        className="text-[10px] font-semibold px-2 py-0.5 rounded-[4px]"
                        style={{
                          backgroundColor: `${itemScoreInfo.color}1F`,
                          color: itemScoreInfo.color,
                          border: `1px solid ${itemScoreInfo.color}40`,
                        }}
                      >
                        {itemScoreInfo.label}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="text-xs text-[var(--text-tertiary)]">
              No sections met the strong or excellent threshold.
            </p>
          )}
        </div>

        {/* Improvements Panel */}
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-2.5 h-2.5 rounded-full bg-[var(--accent-orange)]" />
            <h2 className="font-heading font-semibold text-sm text-[var(--text)] uppercase tracking-wider">
              Improvements
            </h2>
          </div>
          {improvements.length > 0 ? (
            <div className="space-y-3">
              {improvements.map((item) => {
                const itemScoreInfo = getScoreDetails(item.score);
                return (
                  <div
                    key={item.sectionName}
                    className="flex items-center justify-between p-3 rounded-[9px] bg-[var(--bg-page)] border border-[var(--border)]"
                  >
                    <span className="text-xs font-medium text-[var(--text)]">
                      {item.name}
                    </span>
                    <div className="flex items-center gap-2">
                      <span className="font-heading font-bold text-xs text-[var(--text)]">
                        {Math.round(item.score)}/100
                      </span>
                      <span
                        className="text-[10px] font-semibold px-2 py-0.5 rounded-[4px]"
                        style={{
                          backgroundColor: `${itemScoreInfo.color}1F`,
                          color: itemScoreInfo.color,
                          border: `1px solid ${itemScoreInfo.color}40`,
                        }}
                      >
                        {itemScoreInfo.label}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="text-xs text-[var(--text-tertiary)]">
              No critical improvements needed across evaluated sections.
            </p>
          )}
        </div>
      </div>

      {/* 8.5 Six Section Bars */}
      <div>
        <div className="mb-4">
          <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
            Dimension Breakdown
          </span>
          <h2 className="font-heading font-bold text-lg text-[var(--text)] mt-1">
            Scoring dimension breakdown
          </h2>
        </div>

        <div className="space-y-3">
          {enrichedSections.map((sec) => (
            <SectionScoreBar
              key={sec.sectionName}
              sectionName={sec.sectionName}
              name={sec.name}
              score={sec.score}
              weight={sec.weight}
              status={sec.status}
              feedback={sec.feedback}
            />
          ))}
        </div>
      </div>

      {/* 8.6 Six Detail Cards */}
      <div>
        <div className="mb-4">
          <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
            Category Details
          </span>
          <h2 className="font-heading font-bold text-lg text-[var(--text)] mt-1">
            Detailed dimension analysis
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {enrichedSections.map((sec) => (
            <SectionDetailCard
              key={sec.sectionName}
              sectionName={sec.sectionName}
              name={sec.name}
              score={sec.score}
              weight={sec.weight}
              status={sec.status}
              description={sec.description}
              icon={sec.icon}
            />
          ))}
        </div>
      </div>

      {/* 8.7 Risks and Keyword Analysis (Side-by-Side) */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
        {/* Risks Panel (Left) */}
        <div className="md:col-span-6 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 space-y-4">
          <div>
            <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
              Compliance Deductions
            </span>
            <h2 className="font-heading font-bold text-base text-[var(--text)] mt-1">
              Detected risks ({risks.length})
            </h2>
          </div>

          {risks.length > 0 ? (
            <div className="space-y-3">
              {risks.map((risk, idx) => (
                <RiskCard
                  key={idx}
                  title={risk.title}
                  severity={risk.severity}
                  description={risk.description}
                  fix={risk.fix}
                  defaultExpanded={idx === 0}
                />
              ))}
            </div>
          ) : (
            <p className="text-xs text-[var(--text-tertiary)] py-4">
              No compliance risks detected.
            </p>
          )}
        </div>

        {/* Keyword Analysis Panel (Right) */}
        <div className="md:col-span-6 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 space-y-5">
          <div>
            <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
              Skills Alignment
            </span>
            <h2 className="font-heading font-bold text-base text-[var(--text)] mt-1">
              Keyword analysis
            </h2>
          </div>

          {/* Matched Keywords */}
          <div>
            <div className="flex items-center justify-between mb-2.5">
              <span className="text-xs font-semibold text-[var(--success)] flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Matched skills ({matchedKeywords.length})</span>
              </span>
            </div>
            {matchedKeywords.length > 0 ? (
              <div className="flex flex-wrap gap-2">
                {matchedKeywords.map((kw, i) => (
                  <KeywordPill key={i} keyword={kw} variant="matched" />
                ))}
              </div>
            ) : (
              <p className="text-xs text-[var(--text-tertiary)]">
                No matched skills found.
              </p>
            )}
          </div>

          {/* Missing Keywords */}
          <div className="pt-4 border-t border-[var(--border)]">
            <div className="flex items-center justify-between mb-2.5">
              <span className="text-xs font-semibold text-[var(--accent-orange)] flex items-center gap-1.5">
                <AlertCircle className="w-3.5 h-3.5" />
                <span>Missing skills ({missingKeywords.length})</span>
              </span>
            </div>
            {missingKeywords.length > 0 ? (
              <div className="flex flex-wrap gap-2">
                {missingKeywords.map((kw, i) => (
                  <KeywordPill key={i} keyword={kw} variant="missing" />
                ))}
              </div>
            ) : (
              <p className="text-xs text-[var(--text-tertiary)]">
                No missing skills detected.
              </p>
            )}
          </div>
        </div>
      </div>

      {/* 8.8 Top Suggestions Row */}
      {suggestions.length > 0 && (
        <div>
          <div className="mb-4">
            <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
              Actionable Feedback
            </span>
            <h2 className="font-heading font-bold text-lg text-[var(--text)] mt-1">
              Top improvement suggestions
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {suggestions.map((sug, idx) => (
              <SuggestionCard
                key={idx}
                priority={sug.priority}
                title={sug.title}
                description={sug.description}
                example={sug.example}
              />
            ))}
          </div>
        </div>
      )}

      {/* 8.9 Re-analyze Card */}
      <CtaCard
        text="Updated your resume? Scan it again."
        buttonText="Re-analyze"
        to="/"
      />

      {/* Fix Prompt Modal dialog */}
      <FixPromptModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        analysisId={id}
      />
    </div>
  );
}
