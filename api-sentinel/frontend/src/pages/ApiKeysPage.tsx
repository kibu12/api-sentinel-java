import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { ApiKey, Application, CreateApiKeyResponse } from '../types';
import { Key, Plus, Trash2, Copy, Check, AlertCircle, ShieldAlert } from 'lucide-react';

export const ApiKeysPage: React.FC = () => {
  const [keys, setKeys] = useState<ApiKey[]>([]);
  const [apps, setApps] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [newKeyResponse, setNewKeyResponse] = useState<CreateApiKeyResponse | null>(null);
  const [copied, setCopied] = useState(false);

  // Form
  const [applicationId, setApplicationId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [keyList, appList] = await Promise.all([
        api.keys.list(),
        api.applications.list()
      ]);
      setKeys(keyList);
      setApps(appList);
      if (appList.length > 0) setApplicationId(appList[0].id);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreateKey = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const resp = await api.keys.create(applicationId);
      setNewKeyResponse(resp);
      setCreateModalOpen(false);
      await fetchData();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleRevokeKey = async (id: string) => {
    if (!confirm('Are you sure you want to revoke this API key? Consuming applications using it will be rejected immediately.')) return;
    try {
      await api.keys.revoke(id);
      await fetchData();
    } catch (err: any) {
      alert(err.message);
    }
  };

  const copyToClipboard = () => {
    if (newKeyResponse) {
      navigator.clipboard.writeText(newKeyResponse.fullSecretKey);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">API Access Keys</h2>
          <p className="text-sm text-gray-400">Cryptographically hashed credentials for authenticating client gateway requests</p>
        </div>
        <button
          onClick={() => setCreateModalOpen(true)}
          disabled={apps.length === 0}
          className="inline-flex items-center space-x-2 px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white text-xs font-semibold shadow-md shadow-sky-500/20 transition disabled:opacity-50"
        >
          <Plus className="w-4 h-4" />
          <span>Generate API Key</span>
        </button>
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500 text-sm">Loading API keys...</div>
      ) : keys.length === 0 ? (
        <div className="glass-panel rounded-xl p-12 text-center">
          <Key className="w-12 h-12 text-gray-600 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-white">No API Keys Generated</h3>
          <p className="text-xs text-gray-400 max-w-sm mx-auto mt-1 mb-4">
            Generate an access key for your application to begin authenticating calls through the gateway.
          </p>
          <button
            onClick={() => setCreateModalOpen(true)}
            disabled={apps.length === 0}
            className="px-4 py-2 rounded-lg bg-sky-500 text-white text-xs font-semibold hover:bg-sky-400 transition"
          >
            Generate Key
          </button>
        </div>
      ) : (
        <div className="glass-panel rounded-xl overflow-hidden">
          <table className="w-full text-left text-xs text-gray-300">
            <thead className="bg-gray-800/60 uppercase text-[10px] text-gray-400 tracking-wider">
              <tr>
                <th className="p-3.5">Key Prefix</th>
                <th className="p-3.5">Application</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5">Created</th>
                <th className="p-3.5">Last Used</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800/60 font-mono">
              {keys.map((k) => (
                <tr key={k.id} className="hover:bg-gray-800/30">
                  <td className="p-3.5 text-white font-semibold">{k.keyPrefix}</td>
                  <td className="p-3.5 font-sans text-gray-300">{k.applicationName}</td>
                  <td className="p-3.5 font-sans">
                    <span
                      className={`inline-flex items-center space-x-1 text-[10px] font-bold uppercase px-2 py-0.5 rounded-full ${
                        k.status === 'ACTIVE'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}
                    >
                      <span>{k.status}</span>
                    </span>
                  </td>
                  <td className="p-3.5 text-gray-400 font-sans text-[11px]">
                    {new Date(k.createdAt).toLocaleDateString()}
                  </td>
                  <td className="p-3.5 text-gray-400 font-sans text-[11px]">
                    {k.lastUsedAt ? new Date(k.lastUsedAt).toLocaleDateString() : 'Never'}
                  </td>
                  <td className="p-3.5 text-right font-sans">
                    {k.status === 'ACTIVE' && (
                      <button
                        onClick={() => handleRevokeKey(k.id)}
                        className="text-xs text-rose-400 hover:text-rose-300 flex items-center space-x-1 ml-auto transition"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        <span>Revoke</span>
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Secret Key Revealed Modal (Shown ONLY once on creation) */}
      {newKeyResponse && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="glass-panel rounded-2xl p-6 max-w-lg w-full border border-sky-500/40">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
                <ShieldAlert className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-white">Save Your API Key</h3>
                <p className="text-xs text-amber-300">Copy this secret key now. You won't be able to see it again!</p>
              </div>
            </div>

            <div className="my-4 p-3 bg-dark-input rounded-xl border border-dark-border flex items-center justify-between font-mono text-xs text-sky-400 break-all">
              <span>{newKeyResponse.fullSecretKey}</span>
              <button
                onClick={copyToClipboard}
                className="ml-3 p-2 rounded-lg bg-sky-500/20 hover:bg-sky-500/30 text-sky-400 transition flex-shrink-0"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>

            <div className="p-3 bg-gray-800/40 rounded-lg text-[11px] text-gray-400">
              For security, Sentinel stores only the SHA-256 cryptographic hash of this key. Inbound requests must pass this value via the <code className="text-white bg-gray-800 px-1 py-0.5 rounded">X-API-Key</code> header.
            </div>

            <div className="mt-5 flex justify-end">
              <button
                onClick={() => setNewKeyResponse(null)}
                className="px-5 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white text-xs font-semibold"
              >
                I Have Saved This Key
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create Key Modal */}
      {createModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="glass-panel rounded-2xl p-6 max-w-md w-full border border-gray-700">
            <h3 className="text-lg font-bold text-white mb-4">Generate API Key</h3>
            <form onSubmit={handleCreateKey} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-gray-300 uppercase mb-1">Target Application</label>
                <select
                  value={applicationId}
                  onChange={(e) => setApplicationId(e.target.value)}
                  className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                >
                  {apps.map((app) => (
                    <option key={app.id} value={app.id}>{app.name} ({app.environment})</option>
                  ))}
                </select>
              </div>

              <div className="pt-3 flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setCreateModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white font-semibold transition disabled:opacity-50"
                >
                  {submitting ? 'Generating...' : 'Generate Secret Key'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
