import Image from "next/image";
"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import {
  Plus,
  Search,
  QrCode,
  Eye,
  Trash2,
  Pencil,
  ExternalLink,
  Download,
  ToggleLeft,
  ToggleRight,
  Filter,
} from "lucide-react";

interface QR {
  id: string;
  title: string;
  targetUrl: string;
  description: string | null;
  qrDataUrl: string | null;
  fgColor: string;
  bgColor: string;
  scanCount: number;
  isActive: boolean;
  category: string | null;
  createdAt: string;
}

export default function QRCodesPage() {
  const [codes, setCodes] = useState<QR[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filterCategory, setFilterCategory] = useState<string>("all");
  const [deleting, setDeleting] = useState<string | null>(null);

  const fetchCodes = useCallback(() => {
    fetch("/api/qrcodes")
      .then((r) => r.json())
      .then((data) => {
        setCodes(data.qrCodes || []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchCodes();
  }, [fetchCodes]);

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to delete this QR code?")) return;
    setDeleting(id);
    // Optimistic update
    setCodes((prev) => prev.filter((c) => c.id !== id));
    try {
      await fetch(`/api/qrcodes/${id}`, { method: "DELETE" });
    } catch {
      fetchCodes(); // Revert on error
    }
    setDeleting(null);
  };

  const handleToggle = async (id: string, currentState: boolean) => {
    // Optimistic update
    setCodes((prev) =>
      prev.map((c) => (c.id === id ? { ...c, isActive: !currentState } : c))
    );
    try {
      await fetch(`/api/qrcodes/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ isActive: !currentState }),
      });
    } catch {
      fetchCodes();
    }
  };

  const handleDownload = (code: QR) => {
    if (!code.qrDataUrl) return;
    const link = document.createElement("a");
    link.download = `${code.title.replace(/\s+/g, "-").toLowerCase()}.png`;
    link.href = code.qrDataUrl;
    link.click();
  };

  const categories = Array.from(new Set(codes.map((c) => c.category).filter(Boolean)));

  const filtered = codes.filter((c) => {
    const matchSearch =
      c.title.toLowerCase().includes(search.toLowerCase()) ||
      c.targetUrl.toLowerCase().includes(search.toLowerCase());
    const matchCategory = filterCategory === "all" || c.category === filterCategory;
    return matchSearch && matchCategory;
  });

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold">QR Codes</h1>
          <p className="text-text-secondary text-sm mt-1">
            {codes.length} code{codes.length !== 1 ? "s" : ""} total
          </p>
        </div>
        <Link
          href="/dashboard/qrcodes/new"
          className="inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-lg shadow-rock-500/20"
        >
          <Plus size={16} />
          Create QR Code
        </Link>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search QR codes..."
            className="pl-9"
          />
        </div>
        {categories.length > 0 && (
          <div className="relative">
            <Filter size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
            <select
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
              className="pl-9 pr-8 appearance-none cursor-pointer min-w-[160px]"
            >
              <option value="all">All Categories</option>
              {categories.map((cat) => (
                <option key={cat} value={cat!}>
                  {cat}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Content */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="glass-card-solid p-5">
              <div className="flex gap-4">
                <div className="skeleton w-20 h-20 shrink-0" />
                <div className="flex-1 space-y-2">
                  <div className="skeleton w-3/4 h-5" />
                  <div className="skeleton w-full h-4" />
                  <div className="skeleton w-1/2 h-4" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="glass-card-solid p-16 flex flex-col items-center justify-center text-center">
          <div className="w-20 h-20 rounded-3xl bg-surface-3 flex items-center justify-center mb-5">
            <QrCode size={36} className="text-text-muted" />
          </div>
          <h3 className="text-lg font-semibold mb-2">
            {codes.length === 0 ? "No QR codes yet" : "No matching results"}
          </h3>
          <p className="text-sm text-text-secondary mb-6 max-w-sm">
            {codes.length === 0
              ? "Create your first QR code to start tracking scans and engaging your audience."
              : "Try a different search term or filter."}
          </p>
          {codes.length === 0 && (
            <Link
              href="/dashboard/qrcodes/new"
              className="inline-flex items-center gap-2 px-6 py-3 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl"
            >
              <Plus size={16} />
              Create Your First QR Code
            </Link>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          {filtered.map((code, idx) => (
            <div
              key={code.id}
              className="glass-card-solid p-5 hover:border-rock-500/20 transition-all group animate-fade-in"
              style={{ animationDelay: `${idx * 0.05}s` }}
            >
              <div className="flex gap-4 mb-4">
                {code.qrDataUrl && (
                  <div className="shrink-0 w-20 h-20 rounded-xl overflow-hidden bg-white p-1.5 shadow-sm">
                    <Image src={code.qrDataUrl} alt={code.title} width={80} height={80} className="w-full h-full object-contain" />
                  </div>
                )}
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="text-sm font-semibold truncate">{code.title}</h3>
                    <button
                      onClick={() => handleToggle(code.id, code.isActive)}
                      className="shrink-0"
                      title={code.isActive ? "Deactivate" : "Activate"}
                    >
                      {code.isActive ? (
                        <ToggleRight size={22} className="text-emerald-400" />
                      ) : (
                        <ToggleLeft size={22} className="text-text-muted" />
                      )}
                    </button>
                  </div>
                  <p className="text-xs text-text-muted truncate mt-0.5">{code.targetUrl}</p>
                  {code.category && (
                    <span className="inline-flex items-center px-2 py-0.5 rounded-md bg-rock-500/10 text-rock-400 text-[10px] font-medium mt-2">
                      {code.category}
                    </span>
                  )}
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-1.5 text-text-secondary">
                  <Eye size={13} />
                  <span className="text-xs font-medium">{code.scanCount.toLocaleString()} scans</span>
                </div>
                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <Link
                    href={`/dashboard/qrcodes/${code.id}`}
                    className="p-2 rounded-lg text-text-muted hover:text-rock-400 hover:bg-rock-500/10 transition-colors"
                    title="Edit"
                  >
                    <Pencil size={14} />
                  </Link>
                  <button
                    onClick={() => handleDownload(code)}
                    className="p-2 rounded-lg text-text-muted hover:text-blue-400 hover:bg-blue-500/10 transition-colors"
                    title="Download"
                  >
                    <Download size={14} />
                  </button>
                  <a
                    href={code.targetUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="p-2 rounded-lg text-text-muted hover:text-emerald-400 hover:bg-emerald-500/10 transition-colors"
                    title="Open URL"
                  >
                    <ExternalLink size={14} />
                  </a>
                  <button
                    onClick={() => handleDelete(code.id)}
                    disabled={deleting === code.id}
                    className="p-2 rounded-lg text-text-muted hover:text-red-400 hover:bg-red-500/10 transition-colors disabled:opacity-50"
                    title="Delete"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
