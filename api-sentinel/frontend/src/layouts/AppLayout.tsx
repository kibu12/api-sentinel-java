import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Shield,
  LayoutDashboard,
  Boxes,
  Globe,
  Key,
  Activity,
  AlertTriangle,
  Sliders,
  ShieldCheck,
  LogOut,
  Menu,
  X,
  User as UserIcon,
  Play
} from 'lucide-react';

export const AppLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/applications', label: 'Applications', icon: Boxes },
    { to: '/apis', label: 'APIs', icon: Globe },
    { to: '/api-keys', label: 'API Keys', icon: Key },
    { to: '/usage', label: 'Usage Logs', icon: Activity },
    { to: '/anomalies', label: 'Anomalies', icon: AlertTriangle },
    { to: '/settings', label: 'Settings', icon: Sliders },
  ];

  if (user?.role === 'ROLE_ADMIN') {
    navItems.push({ to: '/admin', label: 'Platform Admin', icon: ShieldCheck });
  }

  return (
    <div className="min-h-screen bg-[#0B0F19] text-slate-100 flex flex-col md:flex-row">
      {/* Mobile Top Bar */}
      <div className="md:hidden flex items-center justify-between p-4 bg-[#111827] border-b border-gray-800">
        <div className="flex items-center space-x-2">
          <Shield className="w-6 h-6 text-sky-400" />
          <span className="font-bold text-lg tracking-wider text-white">API SENTINEL</span>
        </div>
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="p-2 rounded text-gray-400 hover:text-white"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Sidebar Navigation */}
      <aside
        className={`${
          mobileMenuOpen ? 'block' : 'hidden'
        } md:flex flex-col w-full md:w-64 bg-[#111827]/90 backdrop-blur-md border-r border-gray-800/80 p-5 z-40`}
      >
        <div className="hidden md:flex items-center space-x-3 mb-8 px-2">
          <div className="p-2 rounded-xl bg-sky-500/10 border border-sky-500/30">
            <Shield className="w-6 h-6 text-sky-400" />
          </div>
          <div>
            <h1 className="font-bold text-lg tracking-wider text-white">API SENTINEL</h1>
            <p className="text-xs text-sky-400 font-medium">Real-time Governance</p>
          </div>
        </div>

        <nav className="space-y-1.5 flex-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setMobileMenuOpen(false)}
                className={({ isActive }) =>
                  `flex items-center space-x-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-sky-500/15 text-sky-400 border border-sky-500/30 font-semibold'
                      : 'text-gray-400 hover:text-white hover:bg-gray-800/50'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* User Card & Logout */}
        <div className="pt-4 border-t border-gray-800/80 space-y-3">
          <div className="flex items-center space-x-3 px-2">
            <div className="w-8 h-8 rounded-full bg-slate-800 border border-gray-700 flex items-center justify-center text-xs font-semibold text-sky-400">
              <UserIcon className="w-4 h-4" />
            </div>
            <div className="truncate flex-1">
              <p className="text-xs font-medium text-white truncate">{user?.email}</p>
              <span className="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-sky-950 text-sky-400 border border-sky-800/50">
                {user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Developer'}
              </span>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center space-x-2 py-2 px-3 rounded-lg text-xs text-rose-400 hover:text-white hover:bg-rose-500/20 border border-rose-500/20 transition"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        <header className="hidden md:flex items-center justify-between px-8 py-4 border-b border-gray-800/60 bg-[#0B0F19]/60 backdrop-blur-sm">
          <div className="flex items-center space-x-2 text-xs text-gray-400">
            <span>Environment:</span>
            <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 font-medium border border-emerald-500/30">
              Active Protection Mode
            </span>
          </div>

          <div className="flex items-center space-x-4">
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="text-xs text-gray-400 hover:text-sky-400 transition"
            >
              OpenAPI Docs ↗
            </a>
            <div className="h-4 w-px bg-gray-800" />
            <span className="text-xs text-gray-400">v2.0 Prototype</span>
          </div>
        </header>

        <div className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto">
          <Outlet />
        </div>

        {/* Footer */}
        <footer className="py-4 px-8 border-t border-gray-800/60 text-center text-xs text-gray-400 flex flex-col sm:flex-row items-center justify-between gap-2">
          <div>© {new Date().getFullYear()} API Sentinel. Enterprise Governance Gateway.</div>
          <div className="flex space-x-4">
            <a href="mailto:support@apisentinel.dev" className="hover:text-sky-400 transition">Contact Support</a>
            <a href="/docs/architecture.md" className="hover:text-sky-400 transition">Architecture Spec</a>
          </div>
        </footer>
      </main>
    </div>
  );
};
