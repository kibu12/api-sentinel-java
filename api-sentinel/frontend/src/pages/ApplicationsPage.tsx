import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Application } from '../types';
import { Boxes, Plus, Trash2, Power, AlertCircle, CheckCircle2 } from 'lucide-react';

export const ApplicationsPage: React.FC = () => {
  const [apps, setApps] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [environment, setEnvironment] = useState('DEVELOPMENT');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchApps = async () => {
    try {
      setLoading(true);
      const list = await api.applications.list();
      setApps(list);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApps();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await api.applications.create({ name, environment });
      setModalOpen(false);
      setName('');
      await fetchApps();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (app: Application) => {
    const newStatus = app.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
    try {
      await api.applications.update(app.id, { status: newStatus });
      await fetchApps();
    } catch (err: any) {
      alert(err.message);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this application? All associated APIs and keys will also be removed.')) return;
    try {
      await api.applications.delete(id);
      await fetchApps();
    } catch (err: any) {
      alert(err.message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Consuming Applications</h2>
          <p className="text-sm text-gray-400">Logical applications, services, and workloads consuming protected APIs</p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="inline-flex items-center space-x-2 px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white text-xs font-semibold shadow-md shadow-sky-500/20 transition"
        >
          <Plus className="w-4 h-4" />
          <span>New Application</span>
        </button>
      </div>

      {error && (
        <div className="p-3.5 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs flex items-center space-x-2">
          <AlertCircle className="w-4 h-4" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="text-center py-12 text-gray-500 text-sm">Loading applications...</div>
      ) : apps.length === 0 ? (
        <div className="glass-panel rounded-xl p-12 text-center">
          <Boxes className="w-12 h-12 text-gray-600 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-white">No Applications Found</h3>
          <p className="text-xs text-gray-400 max-w-sm mx-auto mt-1 mb-4">
            Create your first application to begin registering APIs and issuing secure keys.
          </p>
          <button
            onClick={() => setModalOpen(true)}
            className="px-4 py-2 rounded-lg bg-sky-500 text-white text-xs font-semibold hover:bg-sky-400 transition"
          >
            Create Application
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {apps.map((app) => (
            <div key={app.id} className="glass-panel rounded-xl p-5 flex flex-col justify-between glass-panel-hover">
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded bg-gray-800 text-gray-300">
                    {app.environment}
                  </span>
                  <span
                    className={`inline-flex items-center space-x-1 text-[11px] font-medium px-2 py-0.5 rounded-full ${
                      app.status === 'ACTIVE'
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                        : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                    }`}
                  >
                    <span className={`w-1.5 h-1.5 rounded-full ${app.status === 'ACTIVE' ? 'bg-emerald-400' : 'bg-rose-400'}`} />
                    <span>{app.status}</span>
                  </span>
                </div>

                <h3 className="text-lg font-bold text-white mt-3 truncate">{app.name}</h3>
                <p className="text-[11px] text-gray-400 font-mono mt-0.5">ID: {app.id}</p>
                <p className="text-xs text-gray-500 mt-2">
                  Created {new Date(app.createdAt).toLocaleDateString()}
                </p>
              </div>

              <div className="mt-5 pt-4 border-t border-gray-800 flex items-center justify-between">
                <button
                  onClick={() => handleToggleStatus(app)}
                  className="text-xs text-gray-400 hover:text-white flex items-center space-x-1.5 transition"
                >
                  <Power className="w-3.5 h-3.5" />
                  <span>{app.status === 'ACTIVE' ? 'Disable' : 'Enable'}</span>
                </button>
                <button
                  onClick={() => handleDelete(app.id)}
                  className="text-xs text-rose-400 hover:text-rose-300 flex items-center space-x-1.5 transition"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Delete</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="glass-panel rounded-2xl p-6 max-w-md w-full border border-gray-700">
            <h3 className="text-lg font-bold text-white mb-4">Register New Application</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Application Name</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Analytics Pipeline"
                  className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-sm text-white focus:outline-none focus:border-sky-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Environment</label>
                <select
                  value={environment}
                  onChange={(e) => setEnvironment(e.target.value)}
                  className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-sm text-white focus:outline-none focus:border-sky-500"
                >
                  <option value="DEVELOPMENT">Development</option>
                  <option value="STAGING">Staging</option>
                  <option value="PRODUCTION">Production</option>
                </select>
              </div>

              <div className="pt-3 flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-xs font-medium text-gray-300"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-xs font-semibold text-white shadow-md transition disabled:opacity-50"
                >
                  {submitting ? 'Creating...' : 'Create Application'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
