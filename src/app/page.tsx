"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

export default function Home() {
  const router = useRouter();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    fetch("/api/auth/me")
      .then((r) => {
        if (r.ok) router.replace("/dashboard");
        else setChecking(false);
      })
      .catch(() => setChecking(false));
  }, [router]);

  if (checking) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-rock-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen relative overflow-hidden">
      {/* Background Effects */}
      <div className="absolute inset-0 pointer-events-none">
        <div className="absolute top-[-20%] left-[-10%] w-[60vw] h-[60vw] bg-rock-600/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-20%] right-[-10%] w-[50vw] h-[50vw] bg-indigo-600/8 rounded-full blur-[100px]" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[40vw] h-[40vw] bg-purple-500/5 rounded-full blur-[80px]" />
      </div>

      {/* Header */}
      <header className="relative z-10 flex items-center justify-between px-6 md:px-12 py-6">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-rock-500 to-indigo-600 flex items-center justify-center">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round">
              <rect x="3" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="3" width="7" height="7" rx="1" />
              <rect x="3" y="14" width="7" height="7" rx="1" />
              <rect x="14" y="14" width="3" height="3" rx="0.5" />
              <rect x="18" y="18" width="3" height="3" rx="0.5" />
              <rect x="18" y="14" width="3" height="3" rx="0.5" />
              <rect x="14" y="18" width="3" height="3" rx="0.5" />
            </svg>
          </div>
          <span className="text-xl font-bold tracking-tight">Rock QR</span>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => router.push("/login")}
            className="px-5 py-2.5 text-sm font-medium text-text-secondary hover:text-text-primary transition-colors rounded-xl"
          >
            Sign In
          </button>
          <button
            onClick={() => router.push("/register")}
            className="px-5 py-2.5 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-lg shadow-rock-500/20"
          >
            Get Started Free
          </button>
        </div>
      </header>

      {/* Hero */}
      <main className="relative z-10 flex flex-col items-center text-center px-6 pt-16 md:pt-24 pb-20">
        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-glass border border-glass-border text-sm text-rock-300 mb-8 animate-fade-in">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          New 2026 Model — AI-Powered QR Studio
        </div>

        <h1 className="text-5xl md:text-7xl lg:text-8xl font-black tracking-tight leading-[0.95] max-w-5xl animate-fade-in">
          <span className="gradient-text">Smart QR Codes</span>
          <br />
          <span className="text-text-primary">That Rock.</span>
        </h1>

        <p className="mt-6 text-lg md:text-xl text-text-secondary max-w-2xl animate-fade-in" style={{ animationDelay: "0.1s" }}>
          Create beautiful, trackable QR codes with our next-gen studio.
          Real-time analytics, custom designs, and seamless management — all in one place.
        </p>

        <div className="flex flex-col sm:flex-row items-center gap-4 mt-10 animate-fade-in" style={{ animationDelay: "0.2s" }}>
          <button
            onClick={() => router.push("/register")}
            className="px-8 py-4 text-base font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-2xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-xl shadow-rock-500/25 animate-pulse-glow"
          >
            Start Creating — It&apos;s Free
          </button>
          <button
            onClick={async () => {
              const res = await fetch("/api/seed", { method: "POST" });
              if (res.ok) router.push("/dashboard");
            }}
            className="px-8 py-4 text-base font-medium text-text-secondary border border-surface-border rounded-2xl hover:bg-glass hover:text-text-primary transition-all"
          >
            Try Demo Account →
          </button>
        </div>

        {/* Feature Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-24 max-w-5xl w-full animate-fade-in" style={{ animationDelay: "0.3s" }}>
          {[
            {
              icon: "⚡",
              title: "Instant Generation",
              desc: "Create QR codes in milliseconds with our optimized engine. Custom colors, styles, and sizes.",
            },
            {
              icon: "📊",
              title: "Real-Time Analytics",
              desc: "Track every scan with detailed analytics. Location, device, and time — all in real-time.",
            },
            {
              icon: "🎨",
              title: "Custom Design Studio",
              desc: "Match your brand with custom colors, styles, and embedded logos. Stand out with unique codes.",
            },
          ].map((f) => (
            <div key={f.title} className="glass-card-solid p-8 text-left hover:border-rock-500/30 transition-all group">
              <div className="text-3xl mb-4">{f.icon}</div>
              <h3 className="text-lg font-semibold text-text-primary mb-2">{f.title}</h3>
              <p className="text-sm text-text-secondary leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>

        {/* Stats */}
        <div className="flex flex-wrap justify-center gap-12 mt-20 animate-fade-in" style={{ animationDelay: "0.4s" }}>
          {[
            { val: "2.4M+", label: "QR Codes Created" },
            { val: "18M+", label: "Total Scans" },
            { val: "99.9%", label: "Uptime" },
            { val: "142", label: "Countries" },
          ].map((s) => (
            <div key={s.label} className="text-center">
              <div className="text-3xl md:text-4xl font-black gradient-text">{s.val}</div>
              <div className="text-sm text-text-muted mt-1">{s.label}</div>
            </div>
          ))}
        </div>
      </main>

      {/* Footer */}
      <footer className="relative z-10 border-t border-surface-border py-8 px-6 text-center text-sm text-text-muted">
        © 2026 Rock QR Studio. All rights reserved.
      </footer>
    </div>
  );
}
