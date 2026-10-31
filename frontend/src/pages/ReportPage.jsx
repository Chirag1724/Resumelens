import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getAnalysis, downloadReport } from '../api/client';
import FixPromptModal from '../components/FixPromptModal';
import ReportSkeleton from '../components/ReportSkeleton';
import ReportHeader from '../components/ReportHeader';
import ReportNav from '../components/ReportNav';
import OverviewSection from '../components/OverviewSection';
import ActionPlanSection from '../components/ActionPlanSection';
import JobMatchSection from '../components/JobMatchSection';
import ScoreBreakdownSection from '../components/ScoreBreakdownSection';
import ThingsToFixSection from '../components/ThingsToFixSection';
import DetailsSection from '../components/DetailsSection';
import NextStepsSection from '../components/NextStepsSection';
import { AlertTriangle, AlertCircle, ArrowLeft, RotateCcw } from 'lucide-react';

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

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
      document.title = `${data.fileName} - ATS Analysis Report - ResumeLens`;
    } else {
      document.title = 'ATS Analysis Report - ResumeLens';
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
      link.setAttribute(
        'download',
        `${data?.fileName ? data.fileName.replace(/\.[^/.]+$/, '') : 'ats'}-report.pdf`
      );
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

  const copyToClipboard = async (text) => {
    if (navigator.clipboard && window.isSecureContext) {
      try {
        await navigator.clipboard.writeText(text);
        return true;
      } catch {
        // Fallback
      }
    }
    try {
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      textArea.style.top = '-999999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      const success = document.execCommand('copy');
      textArea.remove();
      return success;
    } catch {
      return false;
    }
  };

  const handleCopyLink = async () => {
    const success = await copyToClipboard(window.location.href);
    if (success) {
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2200);
    }
  };

  // Invalid UUID format shows 404 not found
  if (!isValidUuid) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[calc(100vh-10rem)] px-4 text-center">
        <div className="w-14 h-14 rounded-2xl bg-[var(--danger)]/15 border border-[var(--danger)] flex items-center justify-center mb-5 text-[var(--danger)] shadow-[0_0_30px_rgba(239,68,68,0.2)]">
          <AlertTriangle className="w-7 h-7" aria-hidden="true" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold font-heading text-[var(--text)] mb-3">
          Report Not Found
        </h1>
        <p className="text-sm text-[var(--text-secondary)] max-w-md mb-8 leading-relaxed">
          The requested report identifier is invalid. Please check the URL or analyze your resume again.
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2.5 py-2.5 px-5 bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-sm font-semibold rounded-[9px] transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
        >
          <ArrowLeft className="w-4 h-4 text-[var(--text-secondary)]" aria-hidden="true" />
          <span>Return to Scanner</span>
        </Link>
      </div>
    );
  }

  // Loading skeleton state matching the new layout
  if (loading) {
    return <ReportSkeleton />;
  }

  // 404 not found error state
  if (error && error.status === 404) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[calc(100vh-10rem)] px-4 text-center">
        <div className="w-14 h-14 rounded-2xl bg-[var(--danger)]/15 border border-[var(--danger)] flex items-center justify-center mb-5 text-[var(--danger)] shadow-[0_0_30px_rgba(239,68,68,0.2)]">
          <AlertTriangle className="w-7 h-7" aria-hidden="true" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold font-heading text-[var(--text)] mb-3">
          Report Expired or Not Found
        </h1>
        <p className="text-sm text-[var(--text-secondary)] max-w-md mb-8 leading-relaxed">
          No analysis result was found for this report ID. The link may have expired or was typed incorrectly.
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2.5 py-2.5 px-5 bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-sm font-semibold rounded-[9px] transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
        >
          <ArrowLeft className="w-4 h-4" aria-hidden="true" />
          <span>Scan a Resume Now</span>
        </Link>
      </div>
    );
  }

  // Other error state with retry button
  if (error || !data) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[calc(100vh-10rem)] px-4 text-center">
        <div className="w-14 h-14 rounded-2xl bg-[var(--danger)]/15 border border-[var(--danger)] flex items-center justify-center mb-5 text-[var(--danger)] shadow-[0_0_30px_rgba(239,68,68,0.2)]">
          <AlertCircle className="w-7 h-7" aria-hidden="true" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold font-heading text-[var(--text)] mb-3">
          Unable to Load Report
        </h1>
        <p className="text-sm text-[var(--text-secondary)] max-w-md mb-8 leading-relaxed">
          {error?.message || 'An error occurred while fetching your report. Please check your connection and try again.'}
        </p>
        <div className="flex flex-wrap items-center justify-center gap-3">
          <button
            type="button"
            onClick={fetchReport}
            className="inline-flex items-center gap-2 py-2.5 px-5 bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-sm font-semibold rounded-[9px] transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
          >
            <RotateCcw className="w-4 h-4" aria-hidden="true" />
            <span>Try Again</span>
          </button>
          <Link
            to="/"
            className="inline-flex items-center gap-2 py-2.5 px-5 bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text)] text-sm font-semibold rounded-[9px] transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
          >
            <ArrowLeft className="w-4 h-4 text-[var(--text-secondary)]" aria-hidden="true" />
            <span>Return to Scanner</span>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <>
      <main className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10 space-y-10 sm:space-y-12 lg:space-y-16">
        {/* Section 0: Header */}
        <ReportHeader
          data={data}
          reportId={id}
          onImproveClick={() => setIsModalOpen(true)}
          onDownloadPdf={handleDownloadPdf}
          isDownloading={isDownloading}
          onCopyLink={handleCopyLink}
          copySuccess={copySuccess}
        />

        {/* Sticky in-page navigation under the top bar */}
        <ReportNav />

        {/* Section 1: Overview */}
        <OverviewSection data={data} />

        {/* Section 2: Action plan */}
        <ActionPlanSection data={data} />

        {/* Section 3: Job match */}
        <JobMatchSection data={data} />

        {/* Section 4: Score breakdown */}
        <ScoreBreakdownSection data={data} />

        {/* Section 5: Things to fix */}
        <ThingsToFixSection data={data} />

        {/* Section 6: Details */}
        <DetailsSection data={data} />

        {/* Section 7: Next steps */}
        <NextStepsSection onImproveClick={() => setIsModalOpen(true)} />
      </main>

      {/* Improvement Prompt Modal */}
      <FixPromptModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        analysisId={id}
      />
    </>
  );
}
