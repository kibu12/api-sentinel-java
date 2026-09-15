import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import { ApiConfiguration, UsageRecord } from '../types';
import {
  Globe,
  ArrowLeft,
  Activity,
  ShieldCheck,
  Clock,
  Database,
  Sliders,
  DollarSign,
  AlertCircle
} from 'lucide-react';

export const ApiDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [apiConfig, setApiConfig] = useState<ApiConfiguration | null>(null);
  const [records, setRecords] = useState<UsageRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    const fetchApiDetails = async () => {
      try {
        setLoading(true);
        const [config, usagePaged] = await Promise.all([
          api.apis.get(id),
          api.usage.list(0, 15, id)
        ]);
        setApiConfig(config);
        setRecords(usagePaged.content);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchApiDetails();
  }, [id]);

  if (loading) return <div className="text-center py-16 text-gray-500 text-sm">Loading API configuration...</div>;
  if (!apiConfig) return <div className="text-center py-16 text-rose-400 text-sm">API configuration not found</div>;

  const totalRequests = records.length;
  const cacheHits = records.filter((r) => r.cacheHit).length;
  const errors = records.filter((r) => r.statusCode >= 400 || r.rejected).length;
  const avgLatency =
    totalRequests > 0
      ? Math.round(records.reduce((acc, curr) => acc + curr.latencyMs, 0) / totalRequests)
      : 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-3">
        <Link to="/apis" className="p-2 rounded-lg bg-gray-800 text-gray-400 hover:text-white transition">
          <ArrowLeft className="w-4 h-4" />
        </Link>
        <div>
          <div className="flex items-center space-x-2">
            <h2 className="text-2xl font-bold text-white tracking-tight">{apiConfig.name}</h2>
            <span
              className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                apiConfig.status === 'ACTIVE'
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
              }`}
            >
              {apiConfig.status}
            </span>
          </div>
          <p className="text-xs text-gray-400 font-mono mt-0.5">{apiConfig.baseUrl}</p>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
        <div className="glass-panel rounded-xl p-4">
          <span className="text-gray-400 uppercase tracking-wider block">Average Latency</span>
          <span className="text-2xl font-extrabold text-white mt-1 block">{avgLatency} ms</span>
          <span className="text-[10px] text-sky-400">P95 connection target</span>
        </div>
        <div className="glass-panel rounded-xl p-4">
          <span className="text-gray-400 uppercase tracking-wider block">Cache Hits (Sample)</span>
          <span className="text-2xl font-extrabold text-emerald-400 mt-1 block">{cacheHits}</span>
          <span className="text-[10px] text-gray-500">Bypassed upstream</span>
        </div>
        <div className="glass-panel rounded-xl p-4">
          <span className="text-gray-400 uppercase tracking-wider block">Errors / Rejections</span>
          <span className="text-2xl font-extrabold text-amber-400 mt-1 block">{errors}</span>
          <span className="text-[10px] text-gray-500">Rate limits or 5xx</span>
        </div>
        <div className="glass-panel rounded-xl p-4">
          <span className="text-gray-400 uppercase tracking-wider block">Daily Budget</span>
          <span className="text-2xl font-extrabold text-white mt-1 block">${Number(apiConfig.dailyBudget).toFixed(2)}</span>
          <span className="text-[10px] text-gray-500">100% blocking cap</span>
        </div>
      </div>

      {/* Configuration Details */}
      <div className="glass-panel rounded-xl p-6">
        <h3 className="text-base font-semibold text-white mb-4 flex items-center space-x-2">
          <Sliders className="w-4 h-4 text-sky-400" />
          <span>Active Governance Policies</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
          <div className="p-3 bg-gray-800/40 rounded-lg space-y-1">
            <span className="text-gray-400 block font-medium">Rate Limiting</span>
            <span className="text-white font-semibold text-sm">{apiConfig.rateLimitPerMinute} requests / min</span>
            <p className="text-[11px] text-gray-500">Token-bucket algorithm via Bucket4j</p>
          </div>
          <div className="p-3 bg-gray-800/40 rounded-lg space-y-1">
            <span className="text-gray-400 block font-medium">Daily Request Quota</span>
            <span className="text-white font-semibold text-sm">{apiConfig.dailyQuota.toLocaleString()} requests / day</span>
            <p className="text-[11px] text-gray-500">UTC midnight calendar boundary</p>
          </div>
          <div className="p-3 bg-gray-800/40 rounded-lg space-y-1">
            <span className="text-gray-400 block font-medium">Upstream Timeout</span>
            <span className="text-white font-semibold text-sm">{apiConfig.timeoutMs} ms</span>
            <p className="text-[11px] text-gray-500">Resilience4j bounded breaker</p>
          </div>
        </div>
      </div>

      {/* Recent Traffic Samples */}
      <div className="glass-panel rounded-xl p-6">
        <h3 className="text-base font-semibold text-white mb-4">Recent Inbound Traffic Logs</h3>
        {records.length === 0 ? (
          <p className="text-xs text-gray-500">No requests recorded for this API yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-gray-300">
              <thead className="bg-gray-800/60 uppercase text-[10px] text-gray-400 tracking-wider">
                <tr>
                  <th className="p-2.5">Request ID</th>
                  <th className="p-2.5">Status</th>
                  <th className="p-2.5">Latency</th>
                  <th className="p-2.5">Cost</th>
                  <th className="p-2.5">Cache</th>
                  <th className="p-2.5">Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800/60 font-mono">
                {records.map((r) => (
                  <tr key={r.id} className="hover:bg-gray-800/30">
                    <td className="p-2.5 text-sky-400">{r.requestId}</td>
                    <td className="p-2.5">
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          r.statusCode < 300
                            ? 'bg-emerald-500/10 text-emerald-400'
                            : r.statusCode === 429
                            ? 'bg-amber-500/10 text-amber-400'
                            : 'bg-rose-500/10 text-rose-400'
                        }`}
                      >
                        {r.statusCode}
                      </span>
                    </td>
                    <td className="p-2.5">{r.latencyMs} ms</td>
                    <td className="p-2.5">${Number(r.estimatedCost).toFixed(6)}</td>
                    <td className="p-2.5">
                      {r.cacheHit ? (
                        <span className="text-emerald-400 font-bold">HIT</span>
                      ) : (
                        <span className="text-gray-500">MISS</span>
                      )}
                    </td>
                    <td className="p-2.5 text-gray-400 text-[11px]">
                      {new Date(r.createdAt).toLocaleTimeString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
