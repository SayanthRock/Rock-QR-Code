import Image from "next/image";
"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Sparkles, QrCode } from "lucide-react";
import Link from "next/link";

export default function NewQRCodePage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [targetUrl, setTargetUrl] = useState("");
  const [description, setDescription] = useState("");
  const [fgColor, setFgColor] = useState("#1e1b4b");
  const [bgColor, setBgColor] = useState("#FFFFFF");
  const [category, setCategory] = useState("");
  const [size, setSize] = useState(300);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [preview, setPreview] = useState<string | null>(null);

  const categories = ["Marketing", "Products", "Events", "Social Media", "Restaurant", "Personal", "Other"];

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await fetch("/api/qrcodes", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, targetUrl, description, fgColor, bgColor, size, category: category || null }),
      });

      const data = await res.json();
      if (!res.ok) {
        setError(data.error || "Failed to create QR code");
        setLoading(false);
        return;
      }

      router.push("/dashboard/qrcodes");
    } catch {
      setError("Something went wrong");
      setLoading(false);
    }
  }

  // Live preview generation
  async function generatePreview() {
    if (!targetUrl) return;
    try {
      const res = await fetch("/api/qrcodes/preview", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ targetUrl, fgColor, bgColor, size }),
      });
      const data = await res.json();
      if (data.dataUrl) setPreview(data.dataUrl);
    } catch {
      // ignore preview errors
    }
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Link
          href="/dashboard/qrcodes"
          className="p-2 rounded-xl bg-surface-3 border border-surface-border hover:bg-surface-4 transition-colors"
        >
          <ArrowLeft size={18} className="text-text-secondary" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Create QR Code</h1>
          <p className="text-text-secondary text-sm mt-0.5">Design and generate a new QR code</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          {/* Form */}
          <div className="lg:col-span-3 glass-card-solid p-6 space-y-5">
            {error && (
              <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-sm">
                {error}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Title *</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g., Company Website"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Target URL *</label>
              <input
                type="text"
                value={targetUrl}
                onChange={(e) => setTargetUrl(e.target.value)}
                onBlur={generatePreview}
                placeholder="https://example.com"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Description</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Optional description for this QR code..."
                rows={3}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Category</label>
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="">Select a category</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">Foreground Color</label>
                <div className="flex items-center gap-3">
                  <input
                    type="color"
                    value={fgColor}
                    onChange={(e) => { setFgColor(e.target.value); }}
                    onBlur={generatePreview}
                    className="w-10 h-10 rounded-lg cursor-pointer border-0 p-0"
                  />
                  <input
                    type="text"
                    value={fgColor}
                    onChange={(e) => setFgColor(e.target.value)}
                    className="flex-1"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">Background Color</label>
                <div className="flex items-center gap-3">
                  <input
                    type="color"
                    value={bgColor}
                    onChange={(e) => { setBgColor(e.target.value); }}
                    onBlur={generatePreview}
                    className="w-10 h-10 rounded-lg cursor-pointer border-0 p-0"
                  />
                  <input
                    type="text"
                    value={bgColor}
                    onChange={(e) => setBgColor(e.target.value)}
                    className="flex-1"
                  />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1.5">Size: {size}px</label>
              <input
                type="range"
                min={100}
                max={600}
                step={50}
                value={size}
                onChange={(e) => setSize(Number(e.target.value))}
                className="w-full accent-rock-500 bg-transparent border-0 p-0"
              />
              <div className="flex justify-between text-[11px] text-text-muted mt-1">
                <span>100px</span>
                <span>600px</span>
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="submit"
                disabled={loading}
                className="flex-1 py-3 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-lg shadow-rock-500/20 disabled:opacity-50"
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Creating...
                  </span>
                ) : (
                  <span className="flex items-center justify-center gap-2">
                    <Sparkles size={16} />
                    Generate QR Code
                  </span>
                )}
              </button>
              <Link
                href="/dashboard/qrcodes"
                className="px-6 py-3 text-sm font-medium border border-surface-border text-text-secondary rounded-xl hover:bg-glass hover:text-text-primary transition-all"
              >
                Cancel
              </Link>
            </div>
          </div>

          {/* Preview */}
          <div className="lg:col-span-2 glass-card-solid p-6 flex flex-col items-center justify-center">
            <h3 className="text-sm font-medium text-text-secondary mb-4">Live Preview</h3>
            {preview ? (
              <div className="rounded-2xl overflow-hidden bg-white p-4 shadow-xl">
                <Image src={preview} alt="QR Preview" width={192} height={192} className="object-contain" />
              </div>
            ) : (
              <div className="w-48 h-48 rounded-2xl bg-surface-3 border-2 border-dashed border-surface-border flex flex-col items-center justify-center">
                <QrCode size={40} className="text-text-muted mb-2" />
                <p className="text-xs text-text-muted text-center px-4">
                  Enter a URL and click outside to preview
                </p>
              </div>
            )}
            <p className="text-xs text-text-muted mt-4 text-center">
              {size}×{size}px • Error correction: High
            </p>
          </div>
        </div>
      </form>
    </div>
  );
}
