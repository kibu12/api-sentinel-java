import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Anomaly, PagedResponse } from '../types';
import { AlertTriangle, CheckCircle2, ShieldAlert, Check } from 'lucide-react';

export const AnomaliesPage: React.FC = () => {
  const [data, setData] = useState<PagedResponse<Anomaly> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchAnomalies = async () => {
    try {
      setLoading(true);
      const res = await api.anomalies.list(page, 20);
      setData(res);
    } catch (err) {
      console.error('Failed to load anomalies', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnomalies();
  }, [page]);

  const handleResolve = async (id: string) => {
    try {
      await api.anomalies.resolve(id);
      await fetchAnomalies();
    } catch (err: any) {
      alert(err.message);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Security & Operational Anomalies</h2>
        <p className="text-sm text-gray-400">Automated deterministic detection of traffic surges, error spikes, and budget overflows</p>
      </div>

      {loading ? (
        <div className="text-center py-16 text-gray-500 text-sm">Loading anomalies...</div>
      ) : !data || data.content.length === 0 ? (
        <div className="glass-panel rounded-xl p-12 text-center">
          <ShieldAlert className="w-12 h-12 text-emerald-500/60 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-white">All Gateways Operating Normally</h3>
          <p className="text-xs text-gray-400 max-w-sm mx-auto mt-1">
            No active or historical anomalies recorded. All traffic is within baseline parameters.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {data.content.map((anom) => {
            const isResolved = anom.status === 'RESOLVED';
            return (
              <div
                key={anom.id}
                className={`glass-panel rounded-xl p-5 border-l-4 transition ${
                  isResolved
                    ? 'border-l-gray-600 opacity-60'
                    : anom.severity === 'CRITICAL'
                    ? 'border-l-rose-500'
                    : anom.severity === 'HIGH'
                    ? 'border-l-amber-500'
                    : 'border-l-sky-500'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div className="flex items-start space-x-3">
                    <div className="mt-0.5">
                      {isResolved ? (
                        <CheckCircle2 className="w-5 h-5 text-gray-500" />
                      ) : (
                        <AlertTriangle className="w-5 h-5 text-amber-400" />
                      )}
                    </div>
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="font-bold text-sm text-white">{anom.type}</span>
                        <span
                          className={`text-[10px] uppercase font-extrabold px-2 py-0.5 rounded ${
                            anom.severity === 'CRITICAL'
                              ? 'bg-rose-950 text-rose-400 border border-rose-800'
                              : anom.severity === 'HIGH'
                              ? 'bg-amber-950 text-amber-400 border border-amber-800'
                              : 'bg-sky-950 text-sky-400 border border-sky-800'
                          }`}
                        >
                          {anom.severity}
                        </span>
                        <span className="text-xs text-gray-400">on API: {anom.apiName}</span>
                      </div>
                      <p className="text-xs text-gray-300 mt-1">{anom.description}</p>
                      <div className="flex items-center space-x-4 mt-2 text-[11px] text-gray-400 font-mono">
                        <span>Observed: {Number(anom.observedValue)}</span>
                        <span>Threshold: {Number(anom.threshold)}</span>
                        <span>Detected: {new Date(anom.detectedAt).toLocaleString()}</span>
                        {anom.resolvedAt && (
                          <span className="text-emerald-400">
                            Resolved: {new Date(anom.resolvedAt).toLocaleTimeString()}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {!isResolved && (
                    <button
                      onClick={() => handleResolve(anom.id)}
                      className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 text-xs font-semibold border border-emerald-500/30 transition self-start sm:self-center"
                    >
                      <Check className="w-3.5 h-3.5" />
                      <span>Resolve Alert</span>
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
