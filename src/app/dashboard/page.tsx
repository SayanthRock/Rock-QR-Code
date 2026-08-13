"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { QrCode, BarChart3, Eye, Zap, TrendingUp, ArrowUpRight, Globe } from "lucide-react";

interface Stats {
  totalCodes: number;
  activeCodes: number;
  totalScans: number;
  recentScans: Array<{
    id: string;
    qrCodeId: string;
    qrTitle: string;
    country: string;
    city: string;
    scannedAt: string;
  }>;
  topCodes: Array<{
    id: string;
    title: string;
    scanCount: number;
    targetUrl: string;
    isActive: boolean;
  }>;
  weeklyScans: Array<{ day: string; count: number }>;
}

function StatCard({
  icon: Icon,
  label,
  value,
  change,
  color,
}: {
  icon: React.ComponentType<{ size?: number; className?: string }>;
  label: string;
  value: string | number;
  change?: string;
  color: string;
}) {
  const colorMap: Record<string, string> = {
    purple: "from-rock-500/20 to-rock-600/10 border-rock-500/20 text-rock-400",
    blue: "from-blue-500/20 to-blue-600/10 border-blue-500/20 text-blue-400",
    green: "from-emerald-500/20 to-emerald-600/10 border-emerald-500/20 text-emerald-400",
    amber: "from-amber-500/20 to-amber-600/10 border-amber-500/20 text-amber-400",
  };

  return (
    <div className="glass-card-solid p-6 hover:border-rock-500/20 transition-all group">
      <div className="flex items-start justify-between mb-4">
        <div className={`p-3 rounded-xl bg-gradient-to-br border ${colorMap[color]}`}>
          <Icon size={20} />
        </div>
        {change && (
          <span className="flex items-center gap-1 text-xs font-medium text-emerald-400 bg-emerald-500/10 px-2 py-1 rounded-lg">
            <TrendingUp size={12} /> {change}
          </span>
        )}
      </div>
      <div className="text-3xl font-bold tracking-tight mb-1">{value}</div>
      <div className="text-sm text-text-muted">{label}</div>
    </div>
  );
}

function SkeletonCard() {
  return (
    <div className="glass-card-solid p-6">
      <div className="skeleton w-12 h-12 mb-4" />
      <div className="skeleton w-20 h-8 mb-2" />
      <div className="skeleton w-28 h-4" />
    </div>
  );
}

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/stats")
      .then((r) => r.json())
      .then((data) => {
        setStats(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold">Dashboard</h1>
          <p className="text-text-secondary text-sm mt-1">Your QR code performance overview</p>
        </div>
        <Link
          href="/dashboard/qrcodes/new"
          className="inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-lg shadow-rock-500/20"
        >
          <Zap size={16} />
          New QR Code
        </Link>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {loading ? (
          <>
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </>
        ) : (
          <>
            <StatCard icon={QrCode} label="Total QR Codes" value={stats?.totalCodes ?? 0} change="+12%" color="purple" />
            <StatCard icon={Eye} label="Active Codes" value={stats?.activeCodes ?? 0} color="green" />
            <StatCard icon={BarChart3} label="Total Scans" value={(stats?.totalScans ?? 0).toLocaleString()} change="+24%" color="blue" />
            <StatCard
              icon={TrendingUp}
              label="Avg. Scans/Code"
              value={stats?.totalCodes ? Math.round((stats.totalScans ?? 0) / stats.totalCodes).toLocaleString() : 0}
              color="amber"
            />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Top Performing */}
        <div className="lg:col-span-3 glass-card-solid p-6">
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-semibold">Top Performing QR Codes</h2>
            <Link href="/dashboard/qrcodes" className="text-xs text-rock-400 hover:text-rock-300 font-medium flex items-center gap-1">
              View all <ArrowUpRight size={12} />
            </Link>
          </div>

          {loading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="skeleton w-full h-14" />
              ))}
            </div>
          ) : stats?.topCodes && stats.topCodes.length > 0 ? (
            <div className="space-y-3">
              {stats.topCodes.map((code, idx) => (
                <Link
                  href={`/dashboard/qrcodes/${code.id}`}
                  key={code.id}
                  className="flex items-center gap-4 p-3 rounded-xl hover:bg-glass transition-colors group"
                >
                  <span className="w-7 h-7 rounded-lg bg-surface-3 flex items-center justify-center text-xs font-bold text-text-muted">
                    {idx + 1}
                  </span>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-medium truncate group-hover:text-rock-300 transition-colors">
                      {code.title}
                    </div>
                    <div className="text-xs text-text-muted truncate">{code.targetUrl}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${code.isActive ? "bg-emerald-400" : "bg-text-muted"}`} />
                    <span className="text-sm font-semibold tabular-nums">{code.scanCount.toLocaleString()}</span>
                    <span className="text-xs text-text-muted">scans</span>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <div className="w-14 h-14 rounded-2xl bg-surface-3 flex items-center justify-center mb-4">
                <QrCode size={24} className="text-text-muted" />
              </div>
              <p className="text-sm text-text-secondary mb-1">No QR codes yet</p>
              <p className="text-xs text-text-muted">Create your first QR code to see analytics</p>
            </div>
          )}
        </div>

        {/* Recent Scans */}
        <div className="lg:col-span-2 glass-card-solid p-6">
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-semibold">Recent Scans</h2>
            <Link href="/dashboard/analytics" className="text-xs text-rock-400 hover:text-rock-300 font-medium flex items-center gap-1">
              Analytics <ArrowUpRight size={12} />
            </Link>
          </div>

          {loading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="skeleton w-full h-12" />
              ))}
            </div>
          ) : stats?.recentScans && stats.recentScans.length > 0 ? (
            <div className="space-y-2">
              {stats.recentScans.slice(0, 8).map((scan) => (
                <div key={scan.id} className="flex items-center gap-3 p-2.5 rounded-lg hover:bg-glass transition-colors">
                  <div className="w-8 h-8 rounded-lg bg-surface-3 flex items-center justify-center">
                    <Globe size={14} className="text-text-muted" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-xs font-medium truncate">{scan.qrTitle}</div>
                    <div className="text-[11px] text-text-muted">
                      {scan.city}, {scan.country}
                    </div>
                  </div>
                  <div className="text-[11px] text-text-muted tabular-nums">
                    {new Date(scan.scannedAt).toLocaleDateString("en-US", { month: "short", day: "numeric" })}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <div className="w-14 h-14 rounded-2xl bg-surface-3 flex items-center justify-center mb-4">
                <BarChart3 size={24} className="text-text-muted" />
              </div>
              <p className="text-sm text-text-secondary mb-1">No scans yet</p>
              <p className="text-xs text-text-muted">Scans will appear here in real-time</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
