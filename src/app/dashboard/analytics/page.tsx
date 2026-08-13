"use client";

import { useEffect, useState } from "react";
import { BarChart3, Globe, TrendingUp, Eye, QrCode, ArrowUpRight } from "lucide-react";
import Link from "next/link";

interface Stats {
  totalCodes: number;
  activeCodes: number;
  totalScans: number;
  recentScans: Array<{
    id: string;
    qrCodeId: string;
    qrTitle: string;
    country: string | null;
    city: string | null;
    scannedAt: string;
  }>;
  topCodes: Array<{
    id: string;
    title: string;
    scanCount: number;
    targetUrl: string;
    isActive: boolean;
    fgColor: string;
  }>;
  weeklyScans: Array<{ day: string; count: number }>;
}

function BarChartSimple({ data }: { data: Array<{ day: string; count: number }> }) {
  if (data.length === 0) return null;
  const max = Math.max(...data.map((d) => d.count), 1);

  return (
    <div className="flex items-end gap-2 h-40 mt-4">
      {data.map((d) => {
        const height = (d.count / max) * 100;
        const label = new Date(d.day + "T00:00:00").toLocaleDateString("en-US", { weekday: "short" });
        return (
          <div key={d.day} className="flex-1 flex flex-col items-center gap-1">
            <span className="text-[10px] text-text-muted tabular-nums">{d.count}</span>
            <div
              className="w-full rounded-t-lg bg-gradient-to-t from-rock-600 to-rock-400 transition-all hover:from-rock-500 hover:to-rock-300"
              style={{ height: `${Math.max(height, 4)}%` }}
            />
            <span className="text-[10px] text-text-muted">{label}</span>
          </div>
        );
      })}
    </div>
  );
}

export default function AnalyticsPage() {
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

  // Count scans by country
  const countryMap: Record<string, number> = {};
  stats?.recentScans.forEach((s) => {
    const c = s.country || "Unknown";
    countryMap[c] = (countryMap[c] || 0) + 1;
  });
  const countryCounts = Object.entries(countryMap)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 6);

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl md:text-3xl font-bold">Analytics</h1>
        <p className="text-text-secondary text-sm mt-1">Deep insights into your QR code performance</p>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        {loading ? (
          <>
            {[1, 2, 3].map((i) => (
              <div key={i} className="glass-card-solid p-6">
                <div className="skeleton w-12 h-12 mb-3" />
                <div className="skeleton w-20 h-8 mb-2" />
                <div className="skeleton w-28 h-4" />
              </div>
            ))}
          </>
        ) : (
          <>
            <div className="glass-card-solid p-6">
              <div className="p-3 rounded-xl bg-gradient-to-br from-rock-500/20 to-rock-600/10 border border-rock-500/20 w-fit mb-3">
                <Eye size={20} className="text-rock-400" />
              </div>
              <div className="text-3xl font-bold">{(stats?.totalScans ?? 0).toLocaleString()}</div>
              <div className="text-sm text-text-muted">Total Scans</div>
            </div>
            <div className="glass-card-solid p-6">
              <div className="p-3 rounded-xl bg-gradient-to-br from-blue-500/20 to-blue-600/10 border border-blue-500/20 w-fit mb-3">
                <QrCode size={20} className="text-blue-400" />
              </div>
              <div className="text-3xl font-bold">{stats?.totalCodes ?? 0}</div>
              <div className="text-sm text-text-muted">QR Codes</div>
            </div>
            <div className="glass-card-solid p-6">
              <div className="p-3 rounded-xl bg-gradient-to-br from-emerald-500/20 to-emerald-600/10 border border-emerald-500/20 w-fit mb-3">
                <TrendingUp size={20} className="text-emerald-400" />
              </div>
              <div className="text-3xl font-bold">
                {stats?.totalCodes ? Math.round((stats.totalScans ?? 0) / stats.totalCodes).toLocaleString() : 0}
              </div>
              <div className="text-sm text-text-muted">Avg. Scans per Code</div>
            </div>
          </>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Weekly Chart */}
        <div className="glass-card-solid p-6">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-semibold flex items-center gap-2">
              <BarChart3 size={18} className="text-rock-400" />
              Scan Activity
            </h2>
            <span className="text-xs text-text-muted">Last 30 days</span>
          </div>

          {loading ? (
            <div className="skeleton w-full h-40 mt-4" />
          ) : stats?.weeklyScans && stats.weeklyScans.length > 0 ? (
            <BarChartSimple data={stats.weeklyScans} />
          ) : (
            <div className="flex flex-col items-center justify-center h-40 text-center">
              <BarChart3 size={28} className="text-text-muted mb-2" />
              <p className="text-sm text-text-muted">No scan data available yet</p>
            </div>
          )}
        </div>

        {/* Top Countries */}
        <div className="glass-card-solid p-6">
          <h2 className="text-lg font-semibold flex items-center gap-2 mb-5">
            <Globe size={18} className="text-blue-400" />
            Top Locations
          </h2>

          {loading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="skeleton w-full h-10" />
              ))}
            </div>
          ) : countryCounts.length > 0 ? (
            <div className="space-y-3">
              {countryCounts.map(([country, count]) => {
                const percentage = Math.round((count / (stats?.recentScans.length ?? 1)) * 100);
                return (
                  <div key={country}>
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-sm font-medium">{country}</span>
                      <span className="text-xs text-text-muted">{count} scans ({percentage}%)</span>
                    </div>
                    <div className="w-full h-2 bg-surface-3 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-blue-500 to-rock-500 rounded-full transition-all duration-500"
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-40 text-center">
              <Globe size={28} className="text-text-muted mb-2" />
              <p className="text-sm text-text-muted">No location data available</p>
            </div>
          )}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="glass-card-solid p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-semibold">Recent Scan Activity</h2>
          <span className="text-xs text-text-muted">Last 10 events</span>
        </div>

        {loading ? (
          <div className="space-y-2">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="skeleton w-full h-12" />
            ))}
          </div>
        ) : stats?.recentScans && stats.recentScans.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-surface-border">
                  <th className="text-left py-3 px-3 text-text-muted font-medium text-xs uppercase tracking-wider">QR Code</th>
                  <th className="text-left py-3 px-3 text-text-muted font-medium text-xs uppercase tracking-wider">Location</th>
                  <th className="text-left py-3 px-3 text-text-muted font-medium text-xs uppercase tracking-wider">Time</th>
                  <th className="text-right py-3 px-3 text-text-muted font-medium text-xs uppercase tracking-wider">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {stats.recentScans.map((scan) => (
                  <tr key={scan.id} className="hover:bg-glass transition-colors">
                    <td className="py-3 px-3 font-medium">{scan.qrTitle}</td>
                    <td className="py-3 px-3 text-text-secondary">
                      {scan.city && scan.country ? `${scan.city}, ${scan.country}` : scan.country || "Unknown"}
                    </td>
                    <td className="py-3 px-3 text-text-muted tabular-nums">
                      {new Date(scan.scannedAt).toLocaleDateString("en-US", {
                        month: "short",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </td>
                    <td className="py-3 px-3 text-right">
                      <Link
                        href={`/dashboard/qrcodes/${scan.qrCodeId}`}
                        className="inline-flex items-center gap-1 text-xs text-rock-400 hover:text-rock-300"
                      >
                        View <ArrowUpRight size={12} />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <div className="w-14 h-14 rounded-2xl bg-surface-3 flex items-center justify-center mb-4">
              <BarChart3 size={24} className="text-text-muted" />
            </div>
            <p className="text-sm text-text-secondary mb-1">No scan events yet</p>
            <p className="text-xs text-text-muted">Scans will appear here when your QR codes are used</p>
          </div>
        )}
      </div>
    </div>
  );
}
