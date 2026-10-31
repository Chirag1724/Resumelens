import { useState } from 'react';
import { Link } from 'react-router-dom';
import { FileText, Calendar, ChevronRight, Trash2 } from 'lucide-react';
import { getScoreDetails } from '../lib/score';
import { deleteHistoryItem } from '../api/client';

function formatDate(isoString) {
  if (!isoString) return '';
  try {
    const d = new Date(isoString);
    if (Number.isNaN(d.getTime())) return isoString;
    return d.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return isoString;
  }
}

/**
 * HistoryCard: Displays an individual past evaluation result linking to /report/:id.
 * Shows scan date, resume file name, target job role, and score with tier label.
 * Includes a delete button with a confirm step that removes the card on success.
 */
export default function HistoryCard({ item, onDelete }) {
  const [isConfirming, setIsConfirming] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isDeleted, setIsDeleted] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  if (!item || isDeleted) return null;

  const score = typeof item.overallScore === 'number' ? item.overallScore : 0;
  const scoreInfo = getScoreDetails(score);
  const displayRole = item.jobTitle && item.jobTitle.trim() ? item.jobTitle : 'Resume Scan';
  const displayFileName = item.resumeFileName || 'resume.pdf';
  const formattedDate = formatDate(item.analyzedAt);

  const handleDeleteClick = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setErrorMessage(null);
    setIsConfirming(true);
  };

  const handleCancelDelete = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsConfirming(false);
    setErrorMessage(null);
  };

  const handleConfirmDelete = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDeleting(true);
    setErrorMessage(null);
    try {
      await deleteHistoryItem(item.id);
      setIsDeleted(true);
      if (typeof onDelete === 'function') {
        onDelete(item.id);
      }
    } catch (err) {
      setIsDeleting(false);
      setIsConfirming(false);
      setErrorMessage(err.message || 'Failed to delete report.');
    }
  };

  return (
    <Link
      to={`/report/${item.id}`}
      className="group block bg-[var(--bg-card)] border border-[var(--border)] hover:border-[var(--border-strong)] rounded-[12px] p-5 transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        {/* Left: Role, File and Date */}
        <div className="min-w-0 flex-1">
          <h3 className="font-heading font-semibold text-base text-[var(--text)] group-hover:text-[var(--brand-400)] transition-colors truncate">
            {displayRole}
          </h3>

          <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 mt-2 text-xs">
            <span className="inline-flex items-center gap-1.5 text-[var(--text-secondary)] font-mono truncate max-w-[240px]">
              <FileText className="w-3.5 h-3.5 text-[var(--text-tertiary)] shrink-0" aria-hidden="true" />
              <span className="truncate">{displayFileName}</span>
            </span>

            {formattedDate && (
              <span className="inline-flex items-center gap-1.5 text-[var(--text-tertiary)]">
                <Calendar className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                <span>{formattedDate}</span>
              </span>
            )}
          </div>

          {errorMessage && (
            <div className="mt-2 text-xs text-[var(--danger)] font-medium">
              {errorMessage}
            </div>
          )}
        </div>

        {/* Right: Score, Tier, and Actions */}
        <div className="flex items-center gap-3 sm:gap-4 shrink-0 sm:self-center">
          <div className="text-right">
            <div className="font-heading font-extrabold text-2xl text-[var(--text)] leading-none mb-1">
              {score.toFixed(1)}
            </div>
            <span
              className="inline-block text-[11px] font-semibold px-2 py-0.5 rounded-[6px]"
              style={{
                backgroundColor: `${scoreInfo.color}1F`,
                color: scoreInfo.color,
                border: `1px solid ${scoreInfo.color}40`,
              }}
            >
              {scoreInfo.label}
            </span>
          </div>

          {/* Delete Action / Confirmation */}
          {isConfirming ? (
            <div
              className="flex items-center gap-1.5 p-1 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)]"
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); }}
            >
              <button
                type="button"
                disabled={isDeleting}
                onClick={handleConfirmDelete}
                className="px-2.5 py-1 text-xs font-semibold rounded-[6px] bg-[var(--danger)] text-white hover:opacity-90 transition-opacity cursor-pointer disabled:opacity-50"
              >
                {isDeleting ? 'Deleting...' : 'Confirm'}
              </button>
              <button
                type="button"
                disabled={isDeleting}
                onClick={handleCancelDelete}
                className="px-2 py-1 text-xs font-medium rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors cursor-pointer"
              >
                Cancel
              </button>
            </div>
          ) : (
            <button
              type="button"
              onClick={handleDeleteClick}
              className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--text-tertiary)] hover:text-[var(--danger)] hover:border-[var(--danger)]/40 hover:bg-[var(--danger)]/10 transition-colors cursor-pointer"
              title="Delete report"
              aria-label="Delete report"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}

          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--text-tertiary)] group-hover:text-[var(--text)] group-hover:border-[var(--border-strong)] transition-colors">
            <ChevronRight className="w-4 h-4 transition-transform group-hover:translate-x-0.5" aria-hidden="true" />
          </div>
        </div>
      </div>
    </Link>
  );
}
