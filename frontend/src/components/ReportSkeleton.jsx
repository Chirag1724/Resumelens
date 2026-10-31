export default function ReportSkeleton() {
  return (
    <div className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10 space-y-10 sm:space-y-12 lg:space-y-16 animate-pulse">
      {/* Header Skeleton */}
      <div className="space-y-4">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div className="space-y-3">
            <div className="h-9 w-64 sm:w-80 bg-white/10 rounded-lg" />
            <div className="flex flex-wrap gap-2">
              <div className="h-6 w-32 bg-white/5 rounded-[6px]" />
              <div className="h-6 w-24 bg-white/5 rounded-[6px]" />
              <div className="h-6 w-28 bg-white/5 rounded-[6px]" />
            </div>
          </div>
          <div className="flex gap-2.5">
            <div className="h-10 w-36 bg-white/10 rounded-[9px]" />
            <div className="h-10 w-36 bg-white/5 rounded-[9px]" />
          </div>
        </div>
      </div>

      {/* Nav Skeleton */}
      <div className="h-10 w-full bg-white/5 rounded-[9px]" />

      {/* Overview Skeleton */}
      <div className="space-y-4">
        <div className="space-y-2">
          <div className="h-4 w-20 bg-white/10 rounded" />
          <div className="h-7 w-48 bg-white/10 rounded" />
          <div className="h-4 w-80 bg-white/5 rounded" />
        </div>

        {/* Hero Cards: 5 and 7 cols */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <div className="lg:col-span-5 h-80 bg-[var(--bg-card)] border border-[var(--border)] rounded-[18px] p-6 flex flex-col items-center justify-center gap-4">
            <div className="w-40 h-40 rounded-full bg-white/5" />
            <div className="h-4 w-28 bg-white/5 rounded" />
          </div>
          <div className="lg:col-span-7 h-80 bg-[var(--bg-card)] border border-[var(--border)] rounded-[18px] p-6 flex flex-col justify-between">
            <div className="space-y-3">
              <div className="h-6 w-3/4 bg-white/10 rounded" />
              <div className="h-4 w-full bg-white/5 rounded" />
              <div className="h-4 w-5/6 bg-white/5 rounded" />
            </div>
            <div className="space-y-2 pt-4 border-t border-[var(--border)]">
              <div className="h-4 w-24 bg-white/10 rounded" />
              <div className="h-10 w-full bg-white/5 rounded" />
              <div className="h-10 w-full bg-white/5 rounded" />
            </div>
          </div>
        </div>

        {/* 4 Fact Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mt-6">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="h-28 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 space-y-2"
            >
              <div className="h-3 w-20 bg-white/5 rounded" />
              <div className="h-6 w-28 bg-white/10 rounded" />
              <div className="h-3 w-32 bg-white/5 rounded" />
            </div>
          ))}
        </div>
      </div>

      {/* Action Plan Skeleton */}
      <div className="space-y-4">
        <div className="h-7 w-40 bg-white/10 rounded" />
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div
              key={i}
              className="h-36 bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6"
            />
          ))}
        </div>
      </div>
    </div>
  );
}

