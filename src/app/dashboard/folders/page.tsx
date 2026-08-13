"use client";

import { useEffect, useState } from "react";
import { FolderOpen, Plus, X } from "lucide-react";

interface FolderItem {
  id: string;
  name: string;
  color: string;
  createdAt: string;
}

export default function FoldersPage() {
  const [folders, setFolders] = useState<FolderItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newName, setNewName] = useState("");
  const [newColor, setNewColor] = useState("#6366f1");
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    fetch("/api/folders")
      .then((r) => r.json())
      .then((data) => {
        setFolders(data.folders || []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!newName.trim()) return;
    setCreating(true);

    // Optimistic
    const tempId = "temp-" + Date.now();
    const tempFolder: FolderItem = { id: tempId, name: newName, color: newColor, createdAt: new Date().toISOString() };
    setFolders((prev) => [tempFolder, ...prev]);
    setShowCreate(false);
    setNewName("");
    setNewColor("#6366f1");

    try {
      const res = await fetch("/api/folders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: newName, color: newColor }),
      });
      const data = await res.json();
      if (res.ok) {
        setFolders((prev) => prev.map((f) => (f.id === tempId ? data.folder : f)));
      }
    } catch {
      setFolders((prev) => prev.filter((f) => f.id !== tempId));
    }
    setCreating(false);
  }

  const colorOptions = [
    "#ef4444", "#f97316", "#f59e0b", "#10b981", "#3b82f6",
    "#6366f1", "#8b5cf6", "#ec4899", "#14b8a6", "#64748b",
  ];

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold">Folders</h1>
          <p className="text-text-secondary text-sm mt-1">Organize your QR codes into collections</p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl hover:from-rock-500 hover:to-indigo-500 transition-all shadow-lg shadow-rock-500/20"
        >
          <Plus size={16} />
          New Folder
        </button>
      </div>

      {/* Create Modal */}
      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="w-full max-w-md glass-card-solid p-6 animate-fade-in">
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-semibold">Create Folder</h3>
              <button onClick={() => setShowCreate(false)} className="p-1.5 rounded-lg hover:bg-glass text-text-muted"><X size={18} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1.5">Folder Name</label>
                <input
                  type="text"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="e.g., Marketing Campaign"
                  autoFocus
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-2">Color</label>
                <div className="flex flex-wrap gap-2">
                  {colorOptions.map((c) => (
                    <button
                      key={c}
                      type="button"
                      onClick={() => setNewColor(c)}
                      className={`w-8 h-8 rounded-lg transition-all ${newColor === c ? "ring-2 ring-white ring-offset-2 ring-offset-surface-2 scale-110" : "hover:scale-105"}`}
                      style={{ backgroundColor: c }}
                    />
                  ))}
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="submit"
                  disabled={creating}
                  className="flex-1 py-2.5 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl disabled:opacity-50"
                >
                  Create Folder
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreate(false)}
                  className="px-5 py-2.5 text-sm font-medium border border-surface-border rounded-xl hover:bg-glass transition-colors text-text-secondary"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Folders Grid */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="glass-card-solid p-6">
              <div className="skeleton w-12 h-12 mb-3" />
              <div className="skeleton w-32 h-5 mb-2" />
              <div className="skeleton w-20 h-4" />
            </div>
          ))}
        </div>
      ) : folders.length === 0 ? (
        <div className="glass-card-solid p-16 flex flex-col items-center justify-center text-center">
          <div className="w-20 h-20 rounded-3xl bg-surface-3 flex items-center justify-center mb-5">
            <FolderOpen size={36} className="text-text-muted" />
          </div>
          <h3 className="text-lg font-semibold mb-2">No folders yet</h3>
          <p className="text-sm text-text-secondary mb-6 max-w-sm">
            Create folders to organize your QR codes into logical groups.
          </p>
          <button
            onClick={() => setShowCreate(true)}
            className="inline-flex items-center gap-2 px-6 py-3 text-sm font-semibold bg-gradient-to-r from-rock-600 to-indigo-600 text-white rounded-xl"
          >
            <Plus size={16} />
            Create Your First Folder
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {folders.map((folder, idx) => (
            <div
              key={folder.id}
              className="glass-card-solid p-6 hover:border-rock-500/20 transition-all group cursor-pointer animate-fade-in"
              style={{ animationDelay: `${idx * 0.05}s` }}
            >
              <div
                className="w-12 h-12 rounded-xl flex items-center justify-center mb-4"
                style={{ backgroundColor: folder.color + "20" }}
              >
                <FolderOpen size={22} style={{ color: folder.color }} />
              </div>
              <h3 className="text-base font-semibold mb-1">{folder.name}</h3>
              <p className="text-xs text-text-muted">
                Created {new Date(folder.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
