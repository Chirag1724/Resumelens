import React from 'react';
import { Link } from 'react-router-dom';
import { AlertTriangle, RefreshCw, Home } from 'lucide-react';

/**
 * ErrorBoundary: Catches JavaScript errors anywhere in its child component tree,
 * preventing full application crashes and presenting a recovery interface.
 */
export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(_error, _errorInfo) {
    // Error caught gracefully
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      const pageLabel = this.props.pageName ? ` on the ${this.props.pageName} page` : '';

      return (
        <div className="w-full max-w-2xl mx-auto px-4 py-16 text-center">
          <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[18px] p-8 sm:p-10 shadow-[0_40px_80px_rgba(0,0,0,0.40)]">
            <div className="w-14 h-14 rounded-full bg-[rgba(239,68,68,0.12)] border border-[var(--danger)] flex items-center justify-center mx-auto mb-5 text-[var(--danger)]">
              <AlertTriangle className="w-7 h-7" aria-hidden="true" />
            </div>

            <h1 className="font-heading font-extrabold text-xl sm:text-2xl text-[var(--text)] mb-2">
              Something went wrong
            </h1>

            <p className="text-sm text-[var(--text-secondary)] max-w-md mx-auto mb-6 leading-relaxed">
              An unexpected error occurred{pageLabel}. You can retry loading this section or navigate back to the home page.
            </p>

            <div className="flex flex-wrap items-center justify-center gap-3">
              <button
                type="button"
                onClick={this.handleReset}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-xs font-semibold transition-colors shadow-[var(--shadow-primary-hover)] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
              >
                <RefreshCw className="w-4 h-4" aria-hidden="true" />
                <span>Try again</span>
              </button>

              <Link
                to="/"
                onClick={this.handleReset}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-[9px] bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-xs font-semibold transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
              >
                <Home className="w-4 h-4 text-[var(--text-secondary)]" aria-hidden="true" />
                <span>Return to home</span>
              </Link>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
