import { useState } from 'react';
import { FileText, Briefcase, Clock, Calendar, Sparkles, Download, Copy, Check, Loader2 } from 'lucide-react';

export default function ReportHeader({
  data,
  reportId,
  onImproveClick,
  onDownloadPdf,
  isDownloading,
  onCopyLink,
  copySuccess,
}) {
  const [copiedId, setCopiedId] = useState(false);

  const handleCopyId = async () => {
    if (!reportId) return;
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(reportId);
      } else {
        const textArea = document.createElement('textarea');
        textArea.value = reportId;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        textArea.remove();
      }
      setCopiedId(true);
      setTimeout(() => setCopiedId(false), 2000);
    } catch {
      // quiet fallback
    }
  };

  const formattedDate = data?.analyzedAt
    ? new Date(data.analyzedAt).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      })
    : null;

  const experienceText =
    data?.experienceYears !== undefined && data?.experienceYears !== null && data.experienceYears !== ''
      ? `${data.experienceYears} ${Number(data.experienceYears) === 1 ? 'year' : 'years'} experience`
      : null;

  return (
    <header className="space-y-5">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-3">
          <div className="flex flex-wrap items-center gap-2">
            <h1 className="text-3xl sm:text-4xl font-heading font-extrabold text-[var(--text)] tracking-tight">
              Your resume report
            </h1>
            {reportId && (
              <button
                type="button"
                onClick={handleCopyId}
                title="Copy report ID"
                aria-label={`Copy report ID: ${reportId}`}
                className="font-mono text-xs text-[var(--text-tertiary)] hover:text-[var(--text)] bg-[var(--bg-card)] border border-[var(--border)] hover:border-[var(--border-strong)] px-2.5 py-1 rounded-[6px] inline-flex items-center gap-1.5 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)] cursor-pointer"
              >
                <span>{reportId}</span>
                {copiedId ? (
                  <Check className="w-3 h-3 text-[var(--success)]" aria-hidden="true" />
                ) : (
                  <Copy className="w-3 h-3 text-[var(--text-tertiary)]" aria-hidden="true" />
                )}
              </button>
            )}
          </div>

          {/* Meta chips - hide empty ones */}
          <div className="flex flex-wrap items-center gap-2 text-xs text-[var(--text-secondary)]">
            {data?.fileName && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)]">
                <FileText className="w-3.5 h-3.5 text-[var(--brand-500)]" aria-hidden="true" />
                <span className="font-medium text-[var(--text)]">{data.fileName}</span>
              </span>
            )}
            {data?.targetRole && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)]">
                <Briefcase className="w-3.5 h-3.5 text-[var(--text-tertiary)]" aria-hidden="true" />
                <span>{data.targetRole}</span>
              </span>
            )}
            {experienceText && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)]">
                <Clock className="w-3.5 h-3.5 text-[var(--text-tertiary)]" aria-hidden="true" />
                <span>{experienceText}</span>
              </span>
            )}
            {formattedDate && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)]">
                <Calendar className="w-3.5 h-3.5 text-[var(--text-tertiary)]" aria-hidden="true" />
                <span>{formattedDate}</span>
              </span>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className="flex flex-col sm:items-end gap-2 shrink-0">
          <div className="flex flex-wrap items-center gap-2.5">
            <button
              type="button"
              onClick={onImproveClick}
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] active:scale-[0.98] text-[var(--on-brand)] text-sm font-semibold transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)] focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--bg-page)]"
            >
              <Sparkles className="w-4 h-4" aria-hidden="true" />
              <span>Improve my resume</span>
            </button>

            <button
              type="button"
              onClick={onDownloadPdf}
              disabled={isDownloading}
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-[9px] bg-[var(--bg-card)] hover:bg-[var(--bg-elevated)] active:scale-[0.98] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-sm font-semibold transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)] disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isDownloading ? (
                <Loader2 className="w-4 h-4 animate-spin text-[var(--brand-500)]" aria-hidden="true" />
              ) : (
                <Download className="w-4 h-4 text-[var(--text-secondary)]" aria-hidden="true" />
              )}
              <span>{isDownloading ? 'Downloading...' : 'Download PDF report'}</span>
            </button>

            <button
              type="button"
              onClick={onCopyLink}
              className="inline-flex items-center gap-2 px-3.5 py-2.5 rounded-[9px] bg-[var(--bg-card)] hover:bg-[var(--bg-elevated)] active:scale-[0.98] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-sm font-semibold transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
              aria-label="Copy link to this report"
            >
              {copySuccess ? (
                <>
                  <Check className="w-4 h-4 text-[var(--success)]" aria-hidden="true" />
                  <span className="text-[var(--success)]">Copied</span>
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4 text-[var(--text-secondary)]" aria-hidden="true" />
                  <span>Copy link</span>
                </>
              )}
            </button>
          </div>

          <p className="text-xs text-[var(--text-tertiary)]">
            Anyone with the link can view this report.
          </p>
        </div>
      </div>
    </header>
  );
}

