import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#0B0F19] flex items-center justify-center p-4">
      <div className="glass-panel rounded-2xl p-8 max-w-md w-full text-center border border-gray-800">
        <div className="inline-flex p-3 rounded-2xl bg-amber-500/10 border border-amber-500/20 mb-4">
          <ShieldAlert className="w-10 h-10 text-amber-400" />
        </div>
        <h1 className="text-3xl font-extrabold text-white">404</h1>
        <h2 className="text-lg font-semibold text-gray-200 mt-1">Route Not Found</h2>
        <p className="text-xs text-gray-400 mt-2 mb-6">
          The requested route does not exist in API Sentinel.
        </p>
        <Link
          to="/dashboard"
          className="inline-flex items-center space-x-2 py-2.5 px-5 rounded-lg bg-sky-500 hover:bg-sky-400 text-white font-medium text-xs shadow-lg shadow-sky-500/20 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Return to Dashboard</span>
        </Link>
      </div>
    </div>
  );
};
