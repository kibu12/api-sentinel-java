import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { ApiConfiguration, Application } from '../types';
import { Globe, Plus, Power, ExternalLink, ShieldCheck, Zap, AlertCircle } from 'lucide-react';

export const ApisPage: React.FC = () => {
  const [apis, setApis] = useState<ApiConfiguration[]>([]);
  const [apps, setApps] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form state
  const [applicationId, setApplicationId] = useState('');
  const [name, setName] = useState('');
  const [provider, setProvider] = useState('MOCK');
  const [baseUrl, setBaseUrl] = useState('http://localhost:8080/mock-upstream');
  const [rateLimitPerMinute, setRateLimitPerMinute] = useState(60);
  const [dailyQuota, setDailyQuota] = useState(1000);
  const [dailyBudget, setDailyBudget] = useState(25.0);
  const [timeoutMs, setTimeoutMs] = useState(5000);
  const [cacheEnabled, setCacheEnabled] = useState(true);
  const [cacheTtlSeconds, setCacheTtlSeconds] = useState(60);
  const [submitting, setSubmitting] = useState(false);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [apiList, appList] = await Promise.all([
        api.apis.list(),
        api.applications.list()
      ]);
      setApis(apiList);
      setApps(appList);
      if (appList.length > 0) {
        setApplicationId(appList[0].id);
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await api.apis.create({
        applicationId,
        name,
        provider,
        baseUrl,
        rateLimitPerMinute,
        dailyQuota,
        monthlyQuota: dailyQuota * 20,
        dailyBudget,
        monthlyBudget: dailyBudget * 20,
        timeoutMs,
        cacheEnabled,
        cacheTtlSeconds
      });
      setModalOpen(false);
      setName('');
      await fetchData();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (apiItem: ApiConfiguration) => {
    const newStatus = apiItem.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
    try {
      await api.apis.update(apiItem.id, { status: newStatus });
      await fetchData();
    } catch (err: any) {
      alert(err.message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Protected Upstream APIs</h2>
          <p className="text-sm text-gray-400">Registered target APIs governed by velocity limits, quotas, and budgets</p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          disabled={apps.length === 0}
          className="inline-flex items-center space-x-2 px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white text-xs font-semibold shadow-md shadow-sky-500/20 transition disabled:opacity-50"
        >
          <Plus className="w-4 h-4" />
          <span>Register New API</span>
        </button>
      </div>

      {apps.length === 0 && !loading && (
        <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs">
          Please create an application first before registering an upstream API.
        </div>
      )}

      {loading ? (
        <div className="text-center py-12 text-gray-500 text-sm">Loading APIs...</div>
      ) : apis.length === 0 ? (
        <div className="glass-panel rounded-xl p-12 text-center">
          <Globe className="w-12 h-12 text-gray-600 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-white">No APIs Registered</h3>
          <p className="text-xs text-gray-400 max-w-sm mx-auto mt-1 mb-4">
            Register your first target API (e.g. OpenAI, Gemini, or Mock Upstream) to begin routing through Sentinel.
          </p>
          <button
            onClick={() => setModalOpen(true)}
            disabled={apps.length === 0}
            className="px-4 py-2 rounded-lg bg-sky-500 text-white text-xs font-semibold hover:bg-sky-400 transition"
          >
            Register API
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {apis.map((a) => (
            <div key={a.id} className="glass-panel rounded-xl p-6 glass-panel-hover flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-sky-950 text-sky-400 border border-sky-800/60">
                    {a.provider}
                  </span>
                  <div className="flex items-center space-x-2">
                    <span
                      className={`inline-flex items-center space-x-1 text-[11px] font-medium px-2 py-0.5 rounded-full ${
                        a.status === 'ACTIVE'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}
                    >
                      <span className={`w-1.5 h-1.5 rounded-full ${a.status === 'ACTIVE' ? 'bg-emerald-400' : 'bg-rose-400'}`} />
                      <span>{a.status}</span>
                    </span>
                  </div>
                </div>

                <h3 className="text-lg font-bold text-white mt-3 truncate">{a.name}</h3>
                <p className="text-xs text-gray-400 mt-0.5 truncate font-mono">{a.baseUrl}</p>

                <div className="grid grid-cols-3 gap-2 mt-4 pt-3 border-t border-gray-800 text-[11px]">
                  <div>
                    <span className="text-gray-500 block">Rate Limit</span>
                    <span className="text-white font-semibold">{a.rateLimitPerMinute}/min</span>
                  </div>
                  <div>
                    <span className="text-gray-500 block">Daily Quota</span>
                    <span className="text-white font-semibold">{a.dailyQuota.toLocaleString()}</span>
                  </div>
                  <div>
                    <span className="text-gray-500 block">Daily Budget</span>
                    <span className="text-emerald-400 font-semibold">${Number(a.dailyBudget).toFixed(2)}</span>
                  </div>
                </div>
              </div>

              <div className="mt-5 pt-4 border-t border-gray-800 flex items-center justify-between">
                <button
                  onClick={() => handleToggleStatus(a)}
                  className="text-xs text-gray-400 hover:text-white flex items-center space-x-1.5 transition"
                >
                  <Power className="w-3.5 h-3.5" />
                  <span>{a.status === 'ACTIVE' ? 'Disable API' : 'Activate API'}</span>
                </button>

                <Link
                  to={`/apis/${a.id}`}
                  className="text-xs text-sky-400 hover:text-sky-300 flex items-center space-x-1 transition font-medium"
                >
                  <span>API Details & Health</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Registration Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="glass-panel rounded-2xl p-6 max-w-lg w-full border border-gray-700 max-h-[90vh] overflow-y-auto">
            <h3 className="text-lg font-bold text-white mb-4">Register Upstream API Target</h3>
            <form onSubmit={handleCreate} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-gray-300 uppercase mb-1">Owning Application</label>
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

              <div>
                <label className="block font-semibold text-gray-300 uppercase mb-1">API Name</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. OpenAI GPT-4o Gateway"
                  className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Provider Category</label>
                  <select
                    value={provider}
                    onChange={(e) => setProvider(e.target.value)}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  >
                    <option value="MOCK">Mock Local Provider</option>
                    <option value="OPENAI">OpenAI</option>
                    <option value="GEMINI">Google Gemini</option>
                    <option value="ANTHROPIC">Anthropic</option>
                    <option value="CUSTOM">Custom REST API</option>
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Base URL</label>
                  <input
                    type="text"
                    required
                    value={baseUrl}
                    onChange={(e) => setBaseUrl(e.target.value)}
                    placeholder="https://api.openai.com"
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Rate Limit (/min)</label>
                  <input
                    type="number"
                    min="1"
                    value={rateLimitPerMinute}
                    onChange={(e) => setRateLimitPerMinute(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>

                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Daily Quota</label>
                  <input
                    type="number"
                    min="1"
                    value={dailyQuota}
                    onChange={(e) => setDailyQuota(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>

                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Daily Budget ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={dailyBudget}
                    onChange={(e) => setDailyBudget(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Timeout (ms)</label>
                  <input
                    type="number"
                    min="100"
                    value={timeoutMs}
                    onChange={(e) => setTimeoutMs(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>

                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Cache TTL (sec)</label>
                  <input
                    type="number"
                    min="1"
                    value={cacheTtlSeconds}
                    onChange={(e) => setCacheTtlSeconds(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>
              </div>

              <div className="flex items-center space-x-2 pt-1">
                <input
                  type="checkbox"
                  id="cacheEnabled"
                  checked={cacheEnabled}
                  onChange={(e) => setCacheEnabled(e.target.checked)}
                  className="rounded bg-dark-input text-sky-500"
                />
                <label htmlFor="cacheEnabled" className="text-gray-300 cursor-pointer">
                  Enable Redis response caching for duplicate GET/POST queries
                </label>
              </div>

              <div className="pt-3 flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white font-semibold transition disabled:opacity-50"
                >
                  {submitting ? 'Registering...' : 'Register API Target'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
