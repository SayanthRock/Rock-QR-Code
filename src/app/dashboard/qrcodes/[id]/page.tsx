import Image from "next/image";
"use client";

import { useEffect, useState, use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  ArrowLeft,
  Save,
  Trash2,
  Download,
  ExternalLink,
  Eye,
  Calendar,
  ToggleLeft,
  ToggleRight,
} from "lucide-react";

interface QR {
  id: string;
  title: string;
  targetUrl: string;
  description: string | null;
  qrDataUrl: string | null;
  fgColor: string;
  bgColor: string;
  size: number;
  scanCount: number;
  isActive: boolean;
  category: string | null;
  createdAt: string;
  updatedAt: string;
}

export default function QRCodeDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const [code, setCode] = useState<QR | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [title, setTitle] = useState("");
  const [targetUrl, setTargetUrl] = useState("");
  const [description, setDescription] = useState("");
  const [fgColor, setFgColor] = useState("#000000");
  const [bgColor, setBgColor] = useState("#FFFFFF");
  const [category, setCategory] = useState("");
  const [size, setSize] = useState(300);

  const categories = ["Marketing", "Products", "Events", "Social Media", "Restaurant", "Personal", "Other"];

  useEffect(() => {
    fetch(`/api/qrcodes/${id}`)
      .then((r) => {
        if (!r.ok) throw new Error("Not found");
        return r.json();
      })
      .then((data) => {
        const c = data.qrCode;
        setCode(c);
        setTitle(c.title);
        setTargetUrl(c.targetUrl);
        setDescription(c.description || "");
        setFgColor(c.fgColor);
        setBgColor(c.bgColor);
        setCategory(c.category || "");
        setSize(c.size);
        setLoading(false);
      })
      .catch(() => {
        router.push("/dashboard/qrcodes");
      });
  }, [id, router]);

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSaving(true);

    try {
      const res = await fetch(`/api/qrcodes/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, targetUrl, description, fgColor, bgColor, size, category: category || null }),
      });

      const data = await res.json();
      if (!res.ok) {
        setError(data.error || "Failed to update");
        setSaving(false);
        return;
      }

      setCode(data.qrCode);
      setSuccess("QR code updated successfully!");
      setSaving(false);
      setTimeout(() => setSuccess(""), 3000);
    } catch {
      setError("Something went wrong");
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!confirm("Are you sure you want to permanently delete this QR code?")) return;
    try {
      await fetch(`/api/qrcodes/${id}`, { method: "DELETE" });
      router.push("/dashboard/qrcodes");
    } catch {
      setError("Failed to delete");
    }
  }

  async function handleToggle() {
    if (!code) return;
    const newState = !code.isActive;
    setCode({ ...code, isActive: newState });

    try {
      await fetch(`/api/qrcodes/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ isActive: newState }),
      });
    } catch {
      setCode({ ...code, isActive: !newState });
    }
  }

  const handleDownload = () => {
    if (!code?.qrDataUrl) return;
    const link = document.createElement("a");
    link.download = `${code.title.replace(/\s+/g, "-").toLowerCase()}.png`;
    link.href = code.qrDataUrl;
    link.click();
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto space-y-6">
        <div className="skeleton w-48 h-8" />
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          <div className="lg:col-span-3 skeleton h-96" />
          <div className="lg:col-span-2 skeleton h-96" />
        </div>
      </div>
    );
  }

  if (!code) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link
            href="/dashboard/qrcodes"
            className="p-2 rounded-xl bg-surface-3 border border-surface-border hover:bg-surface-4 transition-colors"
          >
            <ArrowLeft size={18} className="text-text-secondary" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold">{code.title}</h1>
            <p className="text-text-secondary text-sm mt-0.5">Edit and manage your QR code</p>
          </div>
        </div>
        <button
          onClick={handleDelete}
          className="p-2.5 rounded-xl border border-red-500/20 text-red-400 hover:bg-red-500/10 transition-colors"
          title="Delete"
        >
          <Trash2 size={18} />
        </button>
      </div>

      <form onSubmit={handleSave}>
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          {/* Form */}
          <div className="lg:col-span-3 glass-card-solid p-6 space-y-5">
            {error && (
              <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-sm">{error}</div>
            )}
            {success && (
              <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm">{success}</div>
            )}

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Title</label>
              <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Target URL</label>
              <input type="text" value={targetUrl} onChange={(e) => setTargetUrl(e.target.value)} required />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Description</label>
              <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Category</label>
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="">No category</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">FG Color</label>
                <div className="flex items-center gap-3">
                  <input type="color" value={fgColor} onChange={(e) => setFgColor(e.target.value)} className="w-10 h-10 rounded-lg cursor-pointer border-0 p-0" />
                  <input type="text" value={fgColor} onChange={(e) => setFgColor(e.target.value)} className="flex-1" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">BG Color</label>
                <div className="flex items-center gap-3">
                  <input type="color" value={bgColor} onChange={(e) => setBgColor(e.target.value)} className="w-10 h-10 rounded-lg cursor-pointer border-0 p-0" />
                  <input type="text" value={bgColor} onChange={(e) => setBgColor(e.target.value)} className="flex-1" />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Size: {size}px</label>
              <input type="range" min={100} max={600} step={50} value={size} onChange={(e) => setSize(Number(e.target.value))} className="w-full accent-rock-500 bg-transparent border-0 p-0" />
            </div>

            <button
              type="submit"
              disabled={saving}
              className="w-full py-3 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-lg shadow-rock-500/20 disabled:opacity-50"
            >
              {saving ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Saving...
                </span>
              ) : (
                <span className="flex items-center justify-center gap-2">
                  <Save size={16} />
                  Save Changes
                </span>
              )}
            </button>
          </div>

          {/* Preview & Info */}
          <div className="lg:col-span-2 space-y-5">
            <div className="glass-card-solid p-6 flex flex-col items-center">
              {code.qrDataUrl && (
                <div className="rounded-2xl overflow-hidden bg-white p-4 shadow-xl mb-4">
                  <Image src={code.qrDataUrl} alt={code.title} width={192} height={192} className="object-contain" />
                </div>
              )}
              <div className="flex gap-2 w-full">
                <button
                  type="button"
                  onClick={handleDownload}
                  className="flex-1 flex items-center justify-center gap-2 py-2.5 text-xs font-medium border border-surface-border rounded-xl hover:bg-glass transition-colors"
                >
                  <Download size={14} /> Download
                </button>
                <a
                  href={code.targetUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex-1 flex items-center justify-center gap-2 py-2.5 text-xs font-medium border border-surface-border rounded-xl hover:bg-glass transition-colors"
                >
                  <ExternalLink size={14} /> Open URL
                </a>
              </div>
            </div>

            <div className="glass-card-solid p-6 space-y-4">
              <h3 className="text-sm font-semibold">Details</h3>

              <div className="flex items-center justify-between">
                <span className="text-sm text-text-secondary">Status</span>
                <button type="button" onClick={handleToggle} className="flex items-center gap-2">
                  {code.isActive ? (
                    <>
                      <span className="text-xs text-emerald-400 font-medium">Active</span>
                      <ToggleRight size={22} className="text-emerald-400" />
                    </>
                  ) : (
                    <>
                      <span className="text-xs text-text-muted font-medium">Inactive</span>
                      <ToggleLeft size={22} className="text-text-muted" />
                    </>
                  )}
                </button>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm text-text-secondary flex items-center gap-2"><Eye size={14} /> Total Scans</span>
                <span className="text-sm font-semibold">{code.scanCount.toLocaleString()}</span>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm text-text-secondary flex items-center gap-2"><Calendar size={14} /> Created</span>
                <span className="text-xs text-text-muted">
                  {new Date(code.createdAt).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}
                </span>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
