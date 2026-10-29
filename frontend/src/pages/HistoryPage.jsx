import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import {
  Clock,
  ArrowUpRight,
  AlertTriangle,
  RefreshCw,
  Sparkles,
  TrendingUp,
} from 'lucide-react';
import { getHistory } from '../api/client';
import { getScoreDetails } from '../lib/score';
import HistoryCard from '../components/HistoryCard';

function formatChartDate(isoString) {
  if (!isoString) return '';
  try {
    const d = new Date(isoString);
    if (Number.isNaN(d.getTime())) return '';
    return d.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return '';
  }
}

function CustomTooltip({ active, payload }) {
  if (!active || !payload || !payload.length) return null;
  const point = payload[0].payload;
  const scoreInfo = getScoreDetails(point.score);

  return (
    <div className="bg-[var(--bg-elevated)] border border-[var(--border)] rounded-[9px] p-3 shadow-[0_20px_40px_rgba(0,0,0,0.50)] text-xs">
      <div className="text-[var(--text-tertiary)] mb-1">{point.date}</div>
      <div className="font-semibold text-[var(--text)] mb-1 truncate max-w-[200px]">
        {point.jobTitle}
      </div>
      <div className="flex items-center gap-2">
        <span className="font-extrabold text-sm text-[var(--text)]">
          {point.score.toFixed(1)}
        </span>
        <span
          className="text-[10px] font-semibold px-1.5 py-0.5 rounded-[4px]"
          style={{
            backgroundColor: `${scoreInfo.color}1F`,
            color: scoreInfo.color,
            border: `1px solid ${scoreInfo.color}40`,
          }}
        >
          {scoreInfo.label}
        </span>
      </div>
    </div>
  );
}

export default function HistoryPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    document.title = 'Analysis History - ResumeLens';
  }, []);

  const fetchHistoryData = useCallback(() => {
    setLoading(true);
    setError(null);

    getHistory()
      .then((data) => {
        const list = Array.isArray(data) ? data : [];
        setItems(list);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load analysis history.');
        setLoading(false);
      });
  }, []);

  useEffect(() => {
    fetchHistoryData();
  }, [fetchHistoryData]);

  // Chart data: sorted ascending by date (oldest to newest)
  const chartData = [...items]
    .sort((a, b) => new Date(a.analyzedAt) - new Date(b.analyzedAt))
    .map((item) => ({
      date: formatChartDate(item.analyzedAt),
      score: typeof item.overallScore === 'number' ? Math.round(item.overallScore * 10) / 10 : 0,
      jobTitle: item.jobTitle && item.jobTitle.trim() ? item.jobTitle : 'Resume Scan',
      id: item.id,
    }));

  return (
    <div className="w-full max-w-6xl xl:max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-10">
      {/* Page Header */}
      <div className="mb-8">
        <div className="flex items-center gap-2 mb-2">
          <Clock className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
          <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
            Account Evaluations
          </span>
        </div>
        <h1 className="font-heading font-extrabold text-2xl sm:text-3xl text-[var(--text)] tracking-tight">
          Analysis history
        </h1>
        <p className="text-sm text-[var(--text-secondary)] mt-1.5">
          Review previous resume evaluations and track ATS compatibility trends over time.
        </p>
      </div>

      {/* Loading Skeleton State */}
      {loading && (
        <div className="space-y-6" aria-busy="true" aria-label="Loading analysis history">
          {/* Chart card skeleton */}
          <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 animate-pulse">
            <div className="flex items-center justify-between mb-6">
              <div className="space-y-1.5">
                <div className="w-32 h-3.5 bg-[var(--bg-elevated)] rounded" />
                <div className="w-56 h-5 bg-[var(--bg-elevated)] rounded" />
              </div>
              <div className="w-20 h-6 bg-[var(--bg-elevated)] rounded-full" />
            </div>
            {/* Chart line & grid mockup */}
            <div className="h-[200px] w-full flex items-end justify-between gap-3 pt-8 px-4 border-b border-[var(--border)]">
              {[40, 65, 55, 75, 70, 85, 80].map((h, i) => (
                <div key={i} className="flex-1 flex flex-col items-center gap-2">
                  <div
                    className="w-full bg-[rgba(232,96,46,0.15)] rounded-t-[4px] border-t-2 border-[var(--brand-500)]"
                    style={{ height: `${h}%` }}
                  />
                  <div className="w-8 h-2.5 bg-[var(--bg-elevated)] rounded" />
                </div>
              ))}
            </div>
          </div>

          {/* History cards list skeleton */}
          <div className="space-y-3">
            {[...Array(4)].map((_, i) => (
              <div
                key={i}
                className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 animate-pulse"
              >
                <div className="space-y-2 flex-1">
                  <div className="w-48 h-5 bg-[var(--bg-elevated)] rounded" />
                  <div className="flex items-center gap-4">
                    <div className="w-32 h-3.5 bg-[var(--bg-elevated)] rounded" />
                    <div className="w-24 h-3.5 bg-[var(--bg-elevated)] rounded" />
                  </div>
                </div>
                <div className="flex items-center gap-4 shrink-0">
                  <div className="space-y-1 text-right">
                    <div className="w-14 h-6 bg-[var(--bg-elevated)] rounded ml-auto" />
                    <div className="w-20 h-4 bg-[var(--bg-elevated)] rounded" />
                  </div>
                  <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)]" />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Error State */}
      {!loading && error && (
        <div className="bg-[var(--bg-card)] border border-[var(--danger)] rounded-[12px] p-6 sm:p-8 text-center">
          <div className="w-12 h-12 rounded-full bg-[rgba(239,68,68,0.12)] border border-[var(--danger)] flex items-center justify-center mx-auto mb-4 text-[var(--danger)]">
            <AlertTriangle className="w-6 h-6" aria-hidden="true" />
          </div>
          <h2 className="font-heading font-bold text-lg text-[var(--text)] mb-2">
            Failed to load history
          </h2>
          <p className="text-sm text-[var(--text-secondary)] max-w-md mx-auto mb-6">
            {error}
          </p>
          <button
            type="button"
            onClick={fetchHistoryData}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-xs font-semibold transition-colors shadow-[var(--shadow-primary-hover)] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
          >
            <RefreshCw className="w-4 h-4" aria-hidden="true" />
            <span>Try again</span>
          </button>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && items.length === 0 && (
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[18px] p-8 sm:p-12 text-center">
          <div className="w-14 h-14 rounded-full bg-[rgba(232,96,46,0.12)] border border-[rgba(232,96,46,0.25)] flex items-center justify-center mx-auto mb-5 text-[var(--brand-500)]">
            <Sparkles className="w-7 h-7" aria-hidden="true" />
          </div>
          <h2 className="font-heading font-bold text-xl text-[var(--text)] mb-2">
            No previous scans found
          </h2>
          <p className="text-sm text-[var(--text-secondary)] max-w-md mx-auto mb-8 leading-relaxed">
            Upload your resume and a target job description to see your first ATS score and analysis results.
          </p>
          <Link
            to="/"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-sm font-semibold transition-colors shadow-[var(--shadow-primary-hover)] cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
          >
            <span>Analyze your resume</span>
            <ArrowUpRight className="w-4 h-4" aria-hidden="true" />
          </Link>
        </div>
      )}

      {/* Loaded Content */}
      {!loading && !error && items.length > 0 && (
        <div className="space-y-8">
          {/* Score Trend Chart: hidden below 2 results */}
          {items.length >= 2 && (
            <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 sm:p-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
                <div className="flex items-center gap-2">
                  <TrendingUp className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
                  <h2 className="font-heading font-bold text-base text-[var(--text)]">
                    Score progression
                  </h2>
                </div>
                <span className="text-xs text-[var(--text-tertiary)] font-mono">
                  {items.length} evaluations recorded
                </span>
              </div>

              {/* Chart container with fixed height to avoid layout shift */}
              <div className="w-full h-[260px] min-h-[260px]">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart
                    data={chartData}
                    margin={{ top: 12, right: 16, left: -20, bottom: 4 }}
                  >
                    <CartesianGrid
                      stroke="rgba(255, 255, 255, 0.08)"
                      strokeDasharray="3 3"
                      vertical={false}
                    />
                    <XAxis
                      dataKey="date"
                      stroke="var(--text-tertiary)"
                      fontSize={11}
                      tickLine={false}
                      axisLine={{ stroke: 'var(--border)' }}
                    />
                    <YAxis
                      domain={[0, 100]}
                      stroke="var(--text-tertiary)"
                      fontSize={11}
                      tickLine={false}
                      axisLine={{ stroke: 'var(--border)' }}
                    />
                    <Tooltip content={<CustomTooltip />} />
                    <Line
                      type="monotone"
                      dataKey="score"
                      stroke="var(--brand-500)"
                      strokeWidth={2.5}
                      dot={{
                        fill: 'var(--brand-500)',
                        r: 4,
                        stroke: 'var(--bg-card)',
                        strokeWidth: 2,
                      }}
                      activeDot={{
                        r: 6,
                        fill: 'var(--brand-400)',
                        stroke: 'var(--brand-500)',
                        strokeWidth: 2,
                      }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}

          {/* History Cards Section */}
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-heading font-bold text-base text-[var(--text)]">
                Past scans ({items.length})
              </h2>
              <span className="text-xs text-[var(--text-tertiary)]">
                Sorted by newest first
              </span>
            </div>

            <div className="space-y-3">
              {items.map((item) => (
                <HistoryCard key={item.id} item={item} />
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
