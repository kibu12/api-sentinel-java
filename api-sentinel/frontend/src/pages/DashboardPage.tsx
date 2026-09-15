import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { UsageSummary, CostBreakdown, Budget, Anomaly, ApiConfiguration, ApiKey } from '../types';
import {
  Activity,
  DollarSign,
  AlertTriangle,
  Zap,
  Globe,
  TrendingUp,
  ShieldAlert,
  Play,
  CheckCircle2,
  RefreshCw
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar
} from 'recharts';

export const DashboardPage: React.FC = () => {
  const [summary, setSummary] = useState<UsageSummary | null>(null);
  const [costBreakdown, setCostBreakdown] = useState<CostBreakdown[]>([]);
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [recentAnomalies, setRecentAnomalies] = useState<Anomaly[]>([]);
  const [apis, setApis] = useState<ApiConfiguration[]>([]);
  const [keys, setKeys] = useState<ApiKey[]>([]);
  const [loading, setLoading] = useState(true);

  // Quick simulator state
  const [burstCount, setBurstCount] = useState(15);
  const [simulateErrors, setSimulateErrors] = useState(false);
  const [simulating, setSimulating] = useState(false);
  const [simulationResult, setSimulationResult] = useState<any>(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [sum, costs, bgt, anom, apiList, keyList] = await Promise.all([
        api.usage.summary(),
        api.usage.cost(),
        api.budgets.list(),
        api.anomalies.list(0, 5),
        api.apis.list(),
        api.keys.list()
      ]);

      setSummary(sum);
      setCostBreakdown(costs);
      setBudgets(bgt);
      setRecentAnomalies(anom.content);
      setApis(apiList);
      setKeys(keyList);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const runBurstSimulation = async () => {
    if (apis.length === 0) return;
    const targetApi = apis[0];
    const targetKey = keys.length > 0 ? "sen_live_demo_key_1234567890abcdef" : "";

    setSimulating(true);
    setSimulationResult(null);
    try {
      const res = await api.simulator.burst(targetApi.id, targetKey, burstCount, simulateErrors);
      setSimulationResult(res);
      await fetchData(); // Refresh metrics dynamically!
    } catch (err: any) {
      console.error('Simulation error', err);
    } finally {
      setSimulating(false);
    }
  };

  const chartData = costBreakdown.map((item) => ({
    name: item.apiName,
    requests: item.requestCount,
    cost: Number(item.totalCost)
  }));

  return (
    <div className="space-y-6">
      {/* Top Header & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Executive Gateway Dashboard</h2>
          <p className="text-sm text-gray-400">Live operational visibility, quota governance, and spend analytics</p>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="inline-flex items-center space-x-2 px-3.5 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-200 text-xs font-medium border border-gray-700 transition"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Metrics</span>
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="glass-panel rounded-xl p-5 border-l-4 border-l-sky-500">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-400">Total Gateway Traffic</span>
            <Activity className="w-5 h-5 text-sky-400" />
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-white">
              {summary ? summary.totalRequests.toLocaleString() : '0'}
            </span>
            <span className="text-xs text-emerald-400 font-medium">requests</span>
          </div>
          <p className="text-[11px] text-gray-400 mt-1">
            Cache hit rate: <span className="text-sky-300 font-semibold">{summary?.cacheHitPercentage || 0}%</span>
          </p>
        </div>

        <div className="glass-panel rounded-xl p-5 border-l-4 border-l-emerald-500">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-400">Calculated Spend</span>
            <DollarSign className="w-5 h-5 text-emerald-400" />
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-white">
              ${summary ? Number(summary.totalCost).toFixed(4) : '0.0000'}
            </span>
            <span className="text-xs text-gray-400">USD</span>
          </div>
          <p className="text-[11px] text-gray-400 mt-1">Enforced by real-time budget limits</p>
        </div>

        <div className="glass-panel rounded-xl p-5 border-l-4 border-l-amber-500">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-400">Error / Reject Rate</span>
            <Zap className="w-5 h-5 text-amber-400" />
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-white">
              {summary ? summary.errorRatePercentage : '0'}%
            </span>
            <span className="text-xs text-gray-400 font-medium">({summary?.errorCount || 0} failed)</span>
          </div>
          <p className="text-[11px] text-gray-400 mt-1">Includes rate-limits & upstream 5xx</p>
        </div>

        <div className="glass-panel rounded-xl p-5 border-l-4 border-l-rose-500">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-400">Active Anomalies</span>
            <AlertTriangle className="w-5 h-5 text-rose-400" />
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-3xl font-extrabold text-white">
              {summary ? summary.openAnomaliesCount : '0'}
            </span>
            <span className="text-xs text-rose-400 font-medium">unresolved</span>
          </div>
          <p className="text-[11px] text-gray-400 mt-1">
            Protected APIs: <span className="text-white font-semibold">{summary?.activeApisCount || 0}</span>
          </p>
        </div>
      </div>

      {/* Traffic Simulator Panel (Demonstration Engine) */}
      <div className="glass-panel rounded-xl p-6 border border-sky-500/20 bg-gradient-to-r from-sky-950/30 to-gray-900/40">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-start space-x-3">
            <div className="p-2.5 rounded-xl bg-sky-500/20 text-sky-400 border border-sky-500/30">
              <Play className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white">One-Click Traffic & Protection Simulator</h3>
              <p className="text-xs text-gray-400 mt-0.5">
                Generate instant request bursts to demonstrate token-bucket rate-limiting (429), cost tracking, and anomaly detection.
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <select
              value={burstCount}
              onChange={(e) => setBurstCount(Number(e.target.value))}
              className="bg-dark-input border border-dark-border text-xs rounded-lg px-3 py-2 text-white focus:outline-none"
            >
              <option value={5}>5 Requests (Normal Traffic)</option>
              <option value={15}>15 Requests (Trigger Rate-Limit)</option>
              <option value={30}>30 Requests (Trigger Traffic Spike)</option>
            </select>

            <label className="flex items-center space-x-2 text-xs text-gray-300 cursor-pointer">
              <input
                type="checkbox"
                checked={simulateErrors}
                onChange={(e) => setSimulateErrors(e.target.checked)}
                className="rounded bg-dark-input border-dark-border text-sky-500 focus:ring-0"
              />
              <span>Simulate 5xx Errors</span>
            </label>

            <button
              onClick={runBurstSimulation}
              disabled={simulating || apis.length === 0}
              className="px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white text-xs font-semibold flex items-center space-x-2 shadow-md shadow-sky-500/20 transition disabled:opacity-50"
            >
              <Play className={`w-3.5 h-3.5 ${simulating ? 'animate-spin' : ''}`} />
              <span>{simulating ? 'Generating Traffic...' : 'Fire Burst'}</span>
            </button>
          </div>
        </div>

        {simulationResult && (
          <div className="mt-4 pt-4 border-t border-gray-800/80 grid grid-cols-2 sm:grid-cols-5 gap-3 text-xs">
            <div className="p-2.5 rounded-lg bg-gray-800/50">
              <span className="text-gray-400">Total Fired:</span>
              <p className="text-base font-bold text-white">{simulationResult.totalAttempted}</p>
            </div>
            <div className="p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
              <span className="text-emerald-400 font-medium">200 OK Upstream:</span>
              <p className="text-base font-bold text-emerald-400">{simulationResult.successCount}</p>
            </div>
            <div className="p-2.5 rounded-lg bg-amber-500/10 border border-amber-500/20">
              <span className="text-amber-400 font-medium">429 Rate-Limited:</span>
              <p className="text-base font-bold text-amber-400">{simulationResult.rateLimitedCount}</p>
            </div>
            <div className="p-2.5 rounded-lg bg-purple-500/10 border border-purple-500/20">
              <span className="text-purple-400 font-medium">Budget Exceeded:</span>
              <p className="text-base font-bold text-purple-400">{simulationResult.budgetExceededCount || 0}</p>
            </div>
            <div className="p-2.5 rounded-lg bg-rose-500/10 border border-rose-500/20">
              <span className="text-rose-400 font-medium">5xx Failures:</span>
              <p className="text-base font-bold text-rose-400">{simulationResult.errorCount}</p>
            </div>
          </div>
        )}
      </div>

      {/* Charts & Budget Bars */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Cost & Request Distribution Chart */}
        <div className="lg:col-span-2 glass-panel rounded-xl p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-base font-semibold text-white">API Traffic & Cost Distribution</h3>
              <p className="text-xs text-gray-400">Request counts and calculated spend per configured API</p>
            </div>
            <span className="text-xs px-2 py-1 rounded bg-sky-500/10 text-sky-400 border border-sky-500/20 font-medium">
              Live DB Aggregation
            </span>
          </div>

          <div className="h-64 w-full">
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" vertical={false} />
                  <XAxis dataKey="name" stroke="#6B7280" fontSize={11} tickLine={false} />
                  <YAxis stroke="#6B7280" fontSize={11} tickLine={false} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#111827', borderColor: '#374151', borderRadius: '8px', fontSize: '12px' }}
                    itemStyle={{ color: '#E5E7EB' }}
                  />
                  <Bar dataKey="requests" name="Requests" fill="#0284c7" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-gray-500">
                No traffic recorded yet. Fire a burst using the simulator above!
              </div>
            )}
          </div>
        </div>

        {/* Budget Spending Caps */}
        <div className="glass-panel rounded-xl p-6 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-semibold text-white">Active Budget Caps</h3>
              <DollarSign className="w-4 h-4 text-emerald-400" />
            </div>

            <div className="space-y-4">
              {budgets.length > 0 ? (
                budgets.slice(0, 4).map((b) => (
                  <div key={b.id} className="space-y-1.5">
                    <div className="flex justify-between text-xs">
                      <span className="font-medium text-gray-200 truncate max-w-[150px]">{b.apiName} ({b.periodType})</span>
                      <span className="text-gray-400 font-mono">
                        ${b.currentSpend.toFixed(2)} / ${b.limitAmount.toFixed(2)}
                      </span>
                    </div>

                    {/* Progress Bar */}
                    <div className="w-full h-2 rounded-full bg-gray-800 overflow-hidden">
                      <div
                        className={`h-full transition-all duration-500 rounded-full ${
                          b.percentageUsed >= 100
                            ? 'bg-rose-500'
                            : b.percentageUsed >= b.criticalPercent
                            ? 'bg-amber-500'
                            : 'bg-sky-500'
                        }`}
                        style={{ width: `${Math.min(b.percentageUsed, 100)}%` }}
                      />
                    </div>

                    <div className="flex justify-between text-[10px] text-gray-500">
                      <span>{b.percentageUsed}% consumed</span>
                      <span className={b.percentageUsed >= 100 ? 'text-rose-400 font-semibold' : ''}>
                        {b.blockingEnabled && b.percentageUsed >= 100 ? 'BLOCKING ACTIVE' : 'Threshold Monitored'}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-xs text-gray-500">No active budgets registered</p>
              )}
            </div>
          </div>

          <div className="pt-4 border-t border-gray-800 text-[11px] text-gray-400">
            Enforces automatic circuit isolation at 100% cap.
          </div>
        </div>
      </div>

      {/* Recent Anomalies Alert Banner */}
      {recentAnomalies.length > 0 && (
        <div className="glass-panel rounded-xl p-6 border-l-4 border-l-rose-500">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center space-x-2">
              <ShieldAlert className="w-5 h-5 text-rose-400" />
              <h3 className="text-sm font-semibold text-white">Recent Security & Traffic Anomalies</h3>
            </div>
            <a href="/anomalies" className="text-xs text-sky-400 hover:underline">
              View All Anomalies →
            </a>
          </div>

          <div className="space-y-2">
            {recentAnomalies.map((anom) => (
              <div key={anom.id} className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 flex items-center justify-between text-xs">
                <div className="flex items-center space-x-3">
                  <span className="px-2 py-0.5 rounded text-[10px] uppercase font-bold tracking-wider bg-rose-950 text-rose-400 border border-rose-800">
                    {anom.type}
                  </span>
                  <span className="text-gray-200">{anom.description}</span>
                </div>
                <span className="text-[11px] text-gray-400 font-mono">
                  {new Date(anom.detectedAt).toLocaleTimeString()}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
