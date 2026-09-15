import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { AuditLog, PagedResponse } from '../types';
import { ShieldCheck, Users, Boxes, Globe, Activity, ChevronLeft, ChevronRight } from 'lucide-react';

export const AdminPage: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  const [auditLogs, setAuditLogs] = useState<PagedResponse<AuditLog>>({
    content: [],
    pageNumber: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0,
    isLast: true,
  });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      const [st, logs] = await Promise.all([
        api.admin.stats(),
        api.admin.auditLogs(page, 20)
      ]);
      setStats(st);
      setAuditLogs(logs);
    } catch (err) {
      console.error('Failed to load admin stats', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminData();
  }, [page]);

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-3">
        <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/30">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Platform Administration</h2>
          <p className="text-sm text-gray-400">Global system health, tenant statistics, and compliance audit trail</p>
        </div>
      </div>

      {/* Platform Statistics */}
      {stats && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="glass-panel rounded-xl p-5 border-l-4 border-l-purple-500">
            <span className="text-xs font-medium uppercase text-gray-400">Registered Users</span>
            <span className="text-2xl font-bold text-white mt-1 block">{stats.totalUsers}</span>
          </div>
          <div className="glass-panel rounded-xl p-5 border-l-4 border-l-sky-500">
            <span className="text-xs font-medium uppercase text-gray-400">Total Applications</span>
            <span className="text-2xl font-bold text-white mt-1 block">{stats.totalApplications}</span>
          </div>
          <div className="glass-panel rounded-xl p-5 border-l-4 border-l-emerald-500">
            <span className="text-xs font-medium uppercase text-gray-400">Configured APIs</span>
            <span className="text-2xl font-bold text-white mt-1 block">{stats.totalApis}</span>
          </div>
          <div className="glass-panel rounded-xl p-5 border-l-4 border-l-amber-500">
            <span className="text-xs font-medium uppercase text-gray-400">Total Usage Invocations</span>
            <span className="text-2xl font-bold text-white mt-1 block">{stats.totalUsageRecords}</span>
          </div>
        </div>
      )}

      {/* Audit Trail Table */}
      <div className="glass-panel rounded-xl p-6">
        <h3 className="text-base font-semibold text-white mb-4">Security & Management Audit Trail</h3>

        {loading ? (
          <div className="text-center py-12 text-gray-500 text-xs">Loading audit logs...</div>
        ) : auditLogs.content.length === 0 ? (
          <p className="text-xs text-gray-500">No audit logs recorded yet.</p>
        ) : (
          <div className="space-y-4">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-gray-300">
                <thead className="bg-gray-800/60 uppercase text-[10px] text-gray-400 tracking-wider">
                  <tr>
                    <th className="p-3">Action</th>
                    <th className="p-3">Resource</th>
                    <th className="p-3">Details / Metadata</th>
                    <th className="p-3">Timestamp</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800/60 font-mono">
                  {auditLogs.content.map((log) => (
                    <tr key={log.id} className="hover:bg-gray-800/30">
                      <td className="p-3 text-sky-400 font-semibold">{log.action}</td>
                      <td className="p-3 text-gray-300 font-sans">
                        <span className="px-1.5 py-0.5 rounded bg-gray-800 text-[10px] font-mono mr-1.5">
                          {log.resourceType}
                        </span>
                        {log.resourceId}
                      </td>
                      <td className="p-3 text-gray-300 font-sans">{log.metadata || '—'}</td>
                      <td className="p-3 text-gray-400 font-sans text-[11px]">
                        {new Date(log.createdAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination Controls */}
            <div className="flex items-center justify-between text-xs text-gray-400 pt-2">
              <span>Page {auditLogs.pageNumber + 1} of {Math.max(1, auditLogs.totalPages)}</span>
              <div className="flex space-x-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-3 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-white disabled:opacity-30 flex items-center space-x-1"
                >
                  <ChevronLeft className="w-3.5 h-3.5" />
                  <span>Prev</span>
                </button>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={auditLogs.isLast}
                  className="px-3 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-white disabled:opacity-30 flex items-center space-x-1"
                >
                  <span>Next</span>
                  <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
