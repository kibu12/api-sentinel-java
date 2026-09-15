import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { UsageRecord, PagedResponse, ApiConfiguration } from '../types';
import { Activity, Search, Filter, ChevronLeft, ChevronRight, CheckCircle2, AlertTriangle, XCircle } from 'lucide-react';

export const UsagePage: React.FC = () => {
  const [data, setData] = useState<PagedResponse<UsageRecord> | null>(null);
  const [apis, setApis] = useState<ApiConfiguration[]>([]);
  const [selectedApiId, setSelectedApiId] = useState<string>('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchRecords = async () => {
    try {
      setLoading(true);
      const [res, apiList] = await Promise.all([
        api.usage.list(page, 20, selectedApiId || undefined),
        api.apis.list()
      ]);
      setData(res);
      setApis(apiList);
    } catch (err) {
      console.error('Failed to load usage logs', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecords();
  }, [page, selectedApiId]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Real-Time Gateway Usage Logs</h2>
          <p className="text-sm text-gray-400">Complete audit trail of every forwarded and rejected request</p>
        </div>

        {/* Filter */}
        <div className="flex items-center space-x-3 text-xs">
          <Filter className="w-4 h-4 text-gray-400" />
          <select
            value={selectedApiId}
            onChange={(e) => {
              setSelectedApiId(e.target.value);
              setPage(0);
            }}
            className="bg-dark-input border border-dark-border rounded-lg px-3 py-2 text-white focus:outline-none"
          >
            <option value="">All Protected APIs</option>
            {apis.map((a) => (
              <option key={a.id} value={a.id}>{a.name}</option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-16 text-gray-500 text-sm">Loading usage audit logs...</div>
      ) : !data || data.content.length === 0 ? (
        <div className="glass-panel rounded-xl p-12 text-center">
          <Activity className="w-12 h-12 text-gray-600 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-white">No Usage Records Found</h3>
          <p className="text-xs text-gray-400 max-w-sm mx-auto mt-1">
            Send requests through the gateway endpoint to populate usage logs and cost metrics.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="glass-panel rounded-xl overflow-hidden">
            <table className="w-full text-left text-xs text-gray-300">
              <thead className="bg-gray-800/60 uppercase text-[10px] text-gray-400 tracking-wider">
                <tr>
                  <th className="p-3.5">Request ID</th>
                  <th className="p-3.5">API & App</th>
                  <th className="p-3.5">Status</th>
                  <th className="p-3.5">Outcome</th>
                  <th className="p-3.5">Latency</th>
                  <th className="p-3.5">Cost</th>
                  <th className="p-3.5">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800/60 font-mono">
                {data.content.map((rec) => {
                  let badge = 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20';
                  if (rec.rejected) {
                    badge = 'bg-amber-500/10 text-amber-400 border border-amber-500/20';
                  } else if (rec.statusCode >= 500) {
                    badge = 'bg-rose-500/10 text-rose-400 border border-rose-500/20';
                  }

                  return (
                    <tr key={rec.id} className="hover:bg-gray-800/30">
                      <td className="p-3.5 text-sky-400 font-semibold">{rec.requestId}</td>
                      <td className="p-3.5 font-sans">
                        <div className="font-medium text-white truncate max-w-[180px]">{rec.apiName}</div>
                        <div className="text-[10px] text-gray-400">{rec.applicationName}</div>
                      </td>
                      <td className="p-3.5">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${badge}`}>
                          {rec.statusCode}
                        </span>
                      </td>
                      <td className="p-3.5 font-sans">
                        {rec.rejected ? (
                          <span className="text-amber-400 text-[11px] font-medium">
                            Blocked ({rec.rejectionReason || 'POLICY'})
                          </span>
                        ) : rec.cacheHit ? (
                          <span className="text-emerald-400 text-[11px] font-medium">
                            Cache Hit (Saved Upstream)
                          </span>
                        ) : (
                          <span className="text-gray-300 text-[11px]">Forwarded OK</span>
                        )}
                      </td>
                      <td className="p-3.5 text-gray-300">{rec.latencyMs} ms</td>
                      <td className="p-3.5 text-white font-medium">
                        ${Number(rec.estimatedCost).toFixed(6)}
                      </td>
                      <td className="p-3.5 text-gray-400 font-sans text-[11px]">
                        {new Date(rec.createdAt).toLocaleTimeString()}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          <div className="flex items-center justify-between text-xs text-gray-400 px-2">
            <span>
              Showing {data.pageNumber * data.pageSize + 1} to{' '}
              {Math.min((data.pageNumber + 1) * data.pageSize, data.totalElements)} of{' '}
              {data.totalElements} records
            </span>
            <div className="flex space-x-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-3 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-white disabled:opacity-30 transition flex items-center space-x-1"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
                <span>Prev</span>
              </button>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={data.isLast}
                className="px-3 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-white disabled:opacity-30 transition flex items-center space-x-1"
              >
                <span>Next</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
