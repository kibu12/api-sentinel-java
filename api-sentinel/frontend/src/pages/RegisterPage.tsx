import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Lock, Mail, ArrowRight, AlertCircle } from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_USER');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(email, password, role);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex p-3 rounded-2xl bg-sky-500/10 border border-sky-500/20 mb-3">
            <Shield className="w-8 h-8 text-sky-400" />
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Create API Sentinel Account</h1>
          <p className="text-sm text-gray-400 mt-1">Start protecting your upstream APIs and controlling costs</p>
        </div>

        <div className="glass-panel rounded-2xl p-6 sm:p-8">
          {error && (
            <div className="mb-5 flex items-center space-x-2 p-3.5 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 text-sm">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Work Email
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-gray-400 absolute left-3 top-3.5" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="engineer@company.com"
                  className="w-full pl-9 pr-4 py-2.5 bg-dark-input border border-dark-border rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:border-sky-500 transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Password
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-gray-400 absolute left-3 top-3.5" />
                <input
                  type="password"
                  required
                  minLength={6}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Minimum 6 characters"
                  className="w-full pl-9 pr-4 py-2.5 bg-dark-input border border-dark-border rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:border-sky-500 transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Account Role
              </label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                className="w-full px-3 py-2.5 bg-dark-input border border-dark-border rounded-lg text-sm text-white focus:outline-none focus:border-sky-500 transition"
              >
                <option value="ROLE_USER">Developer (Manage owned apps & APIs)</option>
                <option value="ROLE_ADMIN">Administrator (Platform oversight)</option>
              </select>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 px-4 rounded-lg bg-sky-500 hover:bg-sky-400 text-white font-medium text-sm flex items-center justify-center space-x-2 shadow-lg shadow-sky-500/20 transition disabled:opacity-50"
              >
                <span>{loading ? 'Creating account...' : 'Create Account'}</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </form>

          <div className="mt-6 pt-5 border-t border-gray-800 text-center text-xs text-gray-400">
            Already have an account?{' '}
            <Link to="/login" className="text-sky-400 hover:underline font-medium">
              Sign in
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
