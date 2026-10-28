import { useState, useEffect, useRef } from 'react';
import { getFixPrompt } from '../api/client';
import { X, Copy, Check, Sparkles, Loader2, RefreshCw } from 'lucide-react';

/**
 * FixPromptModal: Modal dialog for fetching and copying an AI resume improvement
 * prompt tailored to a selected LaTeX template style.
 *
 * @param {Object} props
 * @param {boolean} props.isOpen - Whether the modal dialog is currently visible
 * @param {Function} props.onClose - Callback triggered when the modal is requested to close
 * @param {string} props.analysisId - UUID of the match result to fetch the fix prompt for
 */
export default function FixPromptModal({ isOpen, onClose, analysisId }) {
  const [templateStyle, setTemplateStyle] = useState('Simple Single-Column');
  const [promptText, setPromptText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [copied, setCopied] = useState(false);

  const modalRef = useRef(null);

  // Fetch fix-prompt when modal opens or templateStyle changes
  useEffect(() => {
    if (!isOpen || !analysisId) return;

    let isMounted = true;
    setLoading(true);
    setError(null);

    getFixPrompt(analysisId, templateStyle)
      .then((data) => {
        if (!isMounted) return;
        // If data is wrapped string or object
        const content = typeof data === 'string' ? data : data?.data || data?.prompt || '';
        setPromptText(content);
        setLoading(false);
      })
      .catch((err) => {
        if (!isMounted) return;
        setError(err.message || 'Failed to generate fix prompt.');
        setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, analysisId, templateStyle]);

  // Handle Escape key
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        e.preventDefault();
        onClose();
        return;
      }

      // Focus trap
      if (e.key === 'Tab' && modalRef.current) {
        const focusableElements = modalRef.current.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        if (focusableElements.length === 0) return;

        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        if (e.shiftKey) {
          if (document.activeElement === firstElement) {
            e.preventDefault();
            lastElement.focus();
          }
        } else {
          if (document.activeElement === lastElement) {
            e.preventDefault();
            firstElement.focus();
          }
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  // Focus the first interactive element when opened
  useEffect(() => {
    if (isOpen && modalRef.current) {
      const firstFocusable = modalRef.current.querySelector('select, button');
      if (firstFocusable) {
        firstFocusable.focus();
      }
    }
  }, [isOpen]);

  if (!isOpen) return null;

  // Copy to clipboard with fallback
  const handleCopy = async () => {
    if (!promptText) return;

    let success = false;
    if (navigator.clipboard && window.isSecureContext) {
      try {
        await navigator.clipboard.writeText(promptText);
        success = true;
      } catch {
        success = false;
      }
    }

    if (!success) {
      try {
        const textArea = document.createElement('textarea');
        textArea.value = promptText;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        success = document.execCommand('copy');
        textArea.remove();
      } catch {
        success = false;
      }
    }

    if (success) {
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="fix-prompt-modal-title"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        ref={modalRef}
        className="w-full max-w-2xl bg-[var(--bg-card)] border border-[var(--border)] rounded-[18px] p-6 shadow-[0_40px_80px_rgba(0,0,0,0.50)] flex flex-col max-h-[90vh] overflow-hidden"
      >
        {/* Header */}
        <div className="flex items-start justify-between gap-4 pb-4 border-b border-[var(--border)]">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Sparkles className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
              <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
                AI Improvement Prompt
              </span>
            </div>
            <h2
              id="fix-prompt-modal-title"
              className="font-heading font-bold text-lg text-[var(--text)]"
            >
              Generate resume fix prompt
            </h2>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-[9px] text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--bg-elevated)] transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
            aria-label="Close dialog"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Template Style Selector */}
        <div className="py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <label
            htmlFor="template-style-select"
            className="text-xs font-medium text-[var(--text-secondary)]"
          >
            Template layout style
          </label>
          <select
            id="template-style-select"
            value={templateStyle}
            onChange={(e) => setTemplateStyle(e.target.value)}
            disabled={loading}
            className="px-3 py-1.5 rounded-[9px] bg-[var(--bg-elevated)] border border-[var(--border)] text-xs text-[var(--text)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] cursor-pointer"
          >
            <option value="Simple Single-Column">Simple Single-Column</option>
            <option value="Awesome-CV">Awesome-CV</option>
            <option value="Deedy Resume">Deedy Resume</option>
          </select>
        </div>

        {/* Content area */}
        <div className="flex-1 overflow-hidden flex flex-col min-h-0 mb-4">
          {loading ? (
            <div className="flex flex-col items-center justify-center p-12 text-[var(--text-secondary)] flex-1">
              <Loader2 className="w-6 h-6 animate-spin text-[var(--brand-500)] mb-3" />
              <span className="text-xs">Generating prompt for {templateStyle}...</span>
            </div>
          ) : error ? (
            <div className="p-4 rounded-[12px] bg-[rgba(239,68,68,0.10)] border border-[var(--danger)] text-xs text-[var(--danger)] flex items-center justify-between">
              <span>{error}</span>
              <button
                type="button"
                onClick={() => setTemplateStyle((s) => s)}
                className="inline-flex items-center gap-1 px-2.5 py-1 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text)] text-xs cursor-pointer"
              >
                <RefreshCw className="w-3 h-3" />
                <span>Retry</span>
              </button>
            </div>
          ) : (
            <pre className="font-mono text-xs text-[var(--text-secondary)] bg-[var(--bg-page)] border border-[var(--border)] rounded-[9px] p-4 overflow-y-auto whitespace-pre-wrap select-all flex-1 leading-relaxed">
              {promptText}
            </pre>
          )}
        </div>

        {/* Note and Actions */}
        <div className="pt-3 border-t border-[var(--border)] flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <p className="text-xs text-[var(--text-tertiary)] leading-relaxed">
            Paste this prompt into an AI assistant to generate your formatted resume.
          </p>

          <div className="flex items-center gap-2 shrink-0">
            <button
              type="button"
              onClick={handleCopy}
              disabled={loading || !promptText}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-xs font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] shadow-[var(--shadow-primary-hover)]"
            >
              {copied ? (
                <>
                  <Check className="w-4 h-4" />
                  <span>Copied!</span>
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4" />
                  <span>Copy prompt</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
