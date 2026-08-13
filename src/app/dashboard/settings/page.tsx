"use client";

import { useEffect, useState } from "react";
import { User, Shield, Bell, Palette } from "lucide-react";

interface UserData {
  id: string;
  name: string;
  email: string;
}

export default function SettingsPage() {
  const [user, setUser] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/api/auth/me")
      .then((r) => r.json())
      .then((data) => {
        setUser(data.user);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const initials = user?.name
    ?.split(" ")
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2) || "?";

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl md:text-3xl font-bold">Settings</h1>
        <p className="text-text-secondary text-sm mt-1">Manage your account and preferences</p>
      </div>

      {loading ? (
        <div className="space-y-6">
          <div className="skeleton w-full h-48" />
          <div className="skeleton w-full h-36" />
        </div>
      ) : (
        <>
          {/* Profile */}
          <div className="glass-card-solid p-6">
            <div className="flex items-center gap-3 mb-6">
              <User size={18} className="text-rock-400" />
              <h2 className="text-lg font-semibold">Profile</h2>
            </div>

            <div className="flex items-center gap-5 mb-6">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-rock-500/30 to-indigo-500/30 flex items-center justify-center text-xl font-bold text-rock-300 border border-rock-500/20">
                {initials}
              </div>
              <div>
                <div className="text-lg font-semibold">{user?.name}</div>
                <div className="text-sm text-text-muted">{user?.email}</div>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">Display Name</label>
                <input type="text" defaultValue={user?.name} />
              </div>
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">Email</label>
                <input type="email" defaultValue={user?.email} disabled className="opacity-60 cursor-not-allowed" />
              </div>
              <button className="px-5 py-2.5 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-lg shadow-rock-500/20">
                Save Changes
              </button>
            </div>
          </div>

          {/* Security */}
          <div className="glass-card-solid p-6">
            <div className="flex items-center gap-3 mb-6">
              <Shield size={18} className="text-emerald-400" />
              <h2 className="text-lg font-semibold">Security</h2>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">Current Password</label>
                <input type="password" placeholder="••••••••" />
              </div>
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">New Password</label>
                <input type="password" placeholder="••••••••" />
              </div>
              <button className="px-5 py-2.5 text-sm font-medium border border-surface-border text-text-secondary rounded-xl hover:bg-glass hover:text-text-primary transition-all">
                Update Password
              </button>
            </div>
          </div>

          {/* Preferences */}
          <div className="glass-card-solid p-6">
            <div className="flex items-center gap-3 mb-6">
              <Palette size={18} className="text-amber-400" />
              <h2 className="text-lg font-semibold">Preferences</h2>
            </div>
            <div className="space-y-4">
              <div className="flex items-center justify-between py-3 border-b border-surface-border">
                <div>
                  <div className="text-sm font-medium">Dark Mode</div>
                  <div className="text-xs text-text-muted">Use dark theme across the application</div>
                </div>
                <div className="w-10 h-6 bg-rock-600 rounded-full relative cursor-pointer">
                  <div className="absolute right-1 top-1 w-4 h-4 bg-white rounded-full" />
                </div>
              </div>
              <div className="flex items-center justify-between py-3 border-b border-surface-border">
                <div>
                  <div className="text-sm font-medium flex items-center gap-2">
                    <Bell size={14} className="text-text-muted" /> Email Notifications
                  </div>
                  <div className="text-xs text-text-muted">Receive scan alerts via email</div>
                </div>
                <div className="w-10 h-6 bg-surface-4 rounded-full relative cursor-pointer">
                  <div className="absolute left-1 top-1 w-4 h-4 bg-text-muted rounded-full" />
                </div>
              </div>
              <div className="flex items-center justify-between py-3">
                <div>
                  <div className="text-sm font-medium">Default QR Style</div>
                  <div className="text-xs text-text-muted">Preferred style for new QR codes</div>
                </div>
                <select className="w-32 text-sm">
                  <option>Square</option>
                  <option>Rounded</option>
                  <option>Dots</option>
                </select>
              </div>
            </div>
          </div>

          {/* Danger Zone */}
          <div className="glass-card-solid p-6 border-red-500/20">
            <h2 className="text-lg font-semibold text-red-400 mb-2">Danger Zone</h2>
            <p className="text-sm text-text-muted mb-4">
              Once you delete your account, there is no going back. Please be certain.
            </p>
            <button className="px-5 py-2.5 text-sm font-medium border border-red-500/30 text-red-400 rounded-xl hover:bg-red-500/10 transition-all">
              Delete Account
            </button>
          </div>
        </>
      )}
    </div>
  );
}
