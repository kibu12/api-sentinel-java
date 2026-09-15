import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Budget, ApiConfiguration } from '../types';
import { Sliders, DollarSign, Check, AlertCircle } from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [loading, setLoading] = useState(true);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [editingBudget, setEditingBudget] = useState<Budget | null>(null);

  // Edit fields
  const [limitAmount, setLimitAmount] = useState<number>(50);
  const [warningPercent, setWarningPercent] = useState<number>(80);
  const [criticalPercent, setCriticalPercent] = useState<number>(90);
  const [blockingEnabled, setBlockingEnabled] = useState<boolean>(true);
  const [saving, setSaving] = useState(false);

  const fetchBudgets = async () => {
    try {
      setLoading(true);
      const list = await api.budgets.list();
      setBudgets(list);
    } catch (err) {
      console.error('Failed to load budgets', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBudgets();
  }, []);

  const startEdit = (b: Budget) => {
    setEditingBudget(b);
    setLimitAmount(b.limitAmount);
    setWarningPercent(b.warningPercent);
    setCriticalPercent(b.criticalPercent);
    setBlockingEnabled(b.blockingEnabled);
    setSuccessMsg(null);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingBudget) return;
    setSaving(true);
    try {
      await api.budgets.update(editingBudget.apiId, {
        periodType: editingBudget.periodType,
        limitAmount,
        warningPercent,
        criticalPercent,
        blockingEnabled
      });
      setSuccessMsg(`Successfully updated budget for ${editingBudget.apiName}`);
      setEditingBudget(null);
      await fetchBudgets();
    } catch (err: any) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Spending & Protection Settings</h2>
        <p className="text-sm text-gray-400">Configure financial spending caps, alert thresholds, and blocking enforcement</p>
      </div>

      {successMsg && (
        <div className="p-3.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center space-x-2">
          <Check className="w-4 h-4" />
          <span>{successMsg}</span>
        </div>
      )}

      {loading ? (
        <div className="text-center py-16 text-gray-500 text-sm">Loading budget configurations...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {budgets.map((b) => (
            <div key={b.id} className="glass-panel rounded-xl p-6 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800/60">
                    {b.periodType} BUDGET
                  </span>
                  <span className="text-xs text-gray-400 font-mono">
                    Spend: ${b.currentSpend.toFixed(4)}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-white mt-3 truncate">{b.apiName}</h3>

                <div className="mt-4 space-y-2 text-xs">
                  <div className="flex justify-between py-1 border-b border-gray-800">
                    <span className="text-gray-400">Spending Cap</span>
                    <span className="text-white font-bold font-mono">${b.limitAmount.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-gray-800">
                    <span className="text-gray-400">Warning Threshold</span>
                    <span className="text-amber-400 font-bold">{b.warningPercent}%</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-gray-800">
                    <span className="text-gray-400">Critical Threshold</span>
                    <span className="text-rose-400 font-bold">{b.criticalPercent}%</span>
                  </div>
                  <div className="flex justify-between py-1">
                    <span className="text-gray-400">100% Traffic Blocking</span>
                    <span className={`font-bold ${b.blockingEnabled ? 'text-emerald-400' : 'text-gray-500'}`}>
                      {b.blockingEnabled ? 'ENABLED' : 'DISABLED'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="mt-5 pt-4 border-t border-gray-800">
                <button
                  onClick={() => startEdit(b)}
                  className="w-full py-2 px-3 rounded-lg bg-sky-500/20 hover:bg-sky-500/30 text-sky-400 text-xs font-semibold border border-sky-500/30 transition"
                >
                  Edit Budget Limits
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Edit Budget Modal */}
      {editingBudget && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="glass-panel rounded-2xl p-6 max-w-md w-full border border-gray-700">
            <h3 className="text-lg font-bold text-white mb-2">
              Configure {editingBudget.periodType} Budget
            </h3>
            <p className="text-xs text-gray-400 mb-4">{editingBudget.apiName}</p>

            <form onSubmit={handleSave} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-gray-300 uppercase mb-1">Limit Amount ($ USD)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={limitAmount}
                  onChange={(e) => setLimitAmount(Number(e.target.value))}
                  className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Warning %</label>
                  <input
                    type="number"
                    min="1"
                    max="100"
                    required
                    value={warningPercent}
                    onChange={(e) => setWarningPercent(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-300 uppercase mb-1">Critical %</label>
                  <input
                    type="number"
                    min="1"
                    max="100"
                    required
                    value={criticalPercent}
                    onChange={(e) => setCriticalPercent(Number(e.target.value))}
                    className="w-full px-3 py-2 bg-dark-input border border-dark-border rounded-lg text-white"
                  />
                </div>
              </div>

              <div className="flex items-center space-x-2 pt-2">
                <input
                  type="checkbox"
                  id="blockingToggle"
                  checked={blockingEnabled}
                  onChange={(e) => setBlockingEnabled(e.target.checked)}
                  className="rounded bg-dark-input text-sky-500"
                />
                <label htmlFor="blockingToggle" className="text-gray-300 cursor-pointer font-medium">
                  Block requests at 100% budget exhaustion
                </label>
              </div>

              <div className="pt-3 flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setEditingBudget(null)}
                  className="px-4 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-4 py-2 rounded-lg bg-sky-500 hover:bg-sky-400 text-white font-semibold transition disabled:opacity-50"
                >
                  {saving ? 'Saving...' : 'Save Settings'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
