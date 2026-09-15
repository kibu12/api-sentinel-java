import {
  ApiResponse,
  PagedResponse,
  User,
  Application,
  ApiConfiguration,
  ApiKey,
  CreateApiKeyResponse,
  UsageRecord,
  UsageSummary,
  CostBreakdown,
  Budget,
  Anomaly,
  AuditLog
} from '../types';

const API_BASE = '/api/v1';

class ApiClient {
  private token: string | null = localStorage.getItem('sentinel_jwt');

  setToken(token: string | null) {
    this.token = token;
    if (token) {
      localStorage.setItem('sentinel_jwt', token);
    } else {
      localStorage.removeItem('sentinel_jwt');
    }
  }

  getToken(): string | null {
    return this.token;
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    const data = await response.json();

    if (!response.ok || data.success === false) {
      const errorMsg = data.error?.message || `Request failed with status ${response.status}`;
      throw new Error(errorMsg);
    }

    return data.data;
  }

  // Authentication
  auth = {
    login: async (email: string, password: string): Promise<{ token: string; user: User }> => {
      const res = await this.request<{ token: string; user: User }>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      this.setToken(res.token);
      return res;
    },
    register: async (email: string, password: string, role?: string): Promise<{ token: string; user: User }> => {
      const res = await this.request<{ token: string; user: User }>('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ email, password, role }),
      });
      this.setToken(res.token);
      return res;
    },
    me: (): Promise<User> => this.request<User>('/auth/me'),
    logout: () => this.setToken(null),
  };

  // Applications
  applications = {
    list: (): Promise<Application[]> => this.request<Application[]>('/applications'),
    get: (id: string): Promise<Application> => this.request<Application>(`/applications/${id}`),
    create: (data: { name: string; environment?: string }): Promise<Application> =>
      this.request<Application>('/applications', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: string, data: Partial<Application>): Promise<Application> =>
      this.request<Application>(`/applications/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id: string): Promise<void> =>
      this.request<void>(`/applications/${id}`, { method: 'DELETE' }),
  };

  // APIs
  apis = {
    list: (): Promise<ApiConfiguration[]> => this.request<ApiConfiguration[]>('/apis'),
    get: (id: string): Promise<ApiConfiguration> => this.request<ApiConfiguration>(`/apis/${id}`),
    create: (data: any): Promise<ApiConfiguration> =>
      this.request<ApiConfiguration>('/apis', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: string, data: any): Promise<ApiConfiguration> =>
      this.request<ApiConfiguration>(`/apis/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id: string): Promise<void> =>
      this.request<void>(`/apis/${id}`, { method: 'DELETE' }),
  };

  // API Keys
  keys = {
    list: (): Promise<ApiKey[]> => this.request<ApiKey[]>('/api-keys'),
    create: (applicationId: string, expiresAt?: string): Promise<CreateApiKeyResponse> =>
      this.request<CreateApiKeyResponse>('/api-keys', {
        method: 'POST',
        body: JSON.stringify({ applicationId, expiresAt }),
      }),
    revoke: (id: string): Promise<void> =>
      this.request<void>(`/api-keys/${id}`, { method: 'DELETE' }),
  };

  // Usage & Metrics
  usage = {
    list: (page = 0, size = 20, apiId?: string): Promise<PagedResponse<UsageRecord>> => {
      const query = new URLSearchParams({ page: String(page), size: String(size) });
      if (apiId) query.append('apiId', apiId);
      return this.request<PagedResponse<UsageRecord>>(`/usage?${query.toString()}`);
    },
    summary: (): Promise<UsageSummary> => this.request<UsageSummary>('/usage/summary'),
    cost: (): Promise<CostBreakdown[]> => this.request<CostBreakdown[]>('/usage/cost'),
  };

  // Budgets
  budgets = {
    list: (): Promise<Budget[]> => this.request<Budget[]>('/budgets'),
    update: (apiId: string, data: any): Promise<Budget[]> =>
      this.request<Budget[]>(`/budgets/${apiId}`, { method: 'PUT', body: JSON.stringify(data) }),
  };

  // Anomalies
  anomalies = {
    list: (page = 0, size = 20): Promise<PagedResponse<Anomaly>> =>
      this.request<PagedResponse<Anomaly>>(`/anomalies?page=${page}&size=${size}`),
    resolve: (id: string): Promise<Anomaly> =>
      this.request<Anomaly>(`/anomalies/${id}/resolve`, { method: 'POST' }),
  };

  // Simulator
  simulator = {
    burst: (apiId: string, apiKey: string, count = 15, simulateErrors = false) =>
      this.request<any>('/simulator/burst', {
        method: 'POST',
        body: JSON.stringify({ apiId, apiKey, count, simulateErrors }),
      }),
  };

  // Admin
  admin = {
    stats: (): Promise<any> => this.request<any>('/admin/stats'),
    auditLogs: (page = 0, size = 20): Promise<PagedResponse<AuditLog>> =>
      this.request<PagedResponse<AuditLog>>(`/admin/audit-logs?page=${page}&size=${size}`),
  };
}

export const api = new ApiClient();
