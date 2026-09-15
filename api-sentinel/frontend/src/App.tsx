import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { AppLayout } from './layouts/AppLayout';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { ApplicationsPage } from './pages/ApplicationsPage';
import { ApisPage } from './pages/ApisPage';
import { ApiDetailPage } from './pages/ApiDetailPage';
import { ApiKeysPage } from './pages/ApiKeysPage';
import { UsagePage } from './pages/UsagePage';
import { AnomaliesPage } from './pages/AnomaliesPage';
import { SettingsPage } from './pages/SettingsPage';
import { AdminPage } from './pages/AdminPage';
import { NotFoundPage } from './pages/NotFoundPage';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isLoading } = useAuth();
  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#0B0F19] flex items-center justify-center text-sm text-gray-400">
        Initializing API Sentinel session...
      </div>
    );
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  if (user?.role !== 'ROLE_ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/404" element={<NotFoundPage />} />

          {/* Authenticated Application Routes */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/applications" element={<ApplicationsPage />} />
            <Route path="/apis" element={<ApisPage />} />
            <Route path="/apis/:id" element={<ApiDetailPage />} />
            <Route path="/api-keys" element={<ApiKeysPage />} />
            <Route path="/usage" element={<UsagePage />} />
            <Route path="/anomalies" element={<AnomaliesPage />} />
            <Route path="/settings" element={<SettingsPage />} />

            {/* Admin Route */}
            <Route
              path="/admin"
              element={
                <AdminRoute>
                  <AdminPage />
                </AdminRoute>
              }
            />
          </Route>

          {/* Unknown / 404 Route */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
