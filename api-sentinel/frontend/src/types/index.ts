export interface User {
  id: string;
  email: string;
  role: string;
  status: string;
  createdAt: string;
}

export interface Application {
  id: string;
  ownerId: string;
  name: string;
  environment: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiConfiguration {
  id: string;
  applicationId: string;
  applicationName: string;
  name: string;
  provider: string;
  baseUrl: string;
  status: string;
  rateLimitPerMinute: number;
  dailyQuota: number;
  monthlyQuota: number;
  dailyBudget: number;
  monthlyBudget: number;
  timeoutMs: number;
  cacheEnabled: boolean;
  cacheTtlSeconds: number;
  createdAt: string;
  updatedAt: string;
}

export interface ApiKey {
  id: string;
  applicationId: string;
  applicationName: string;
  keyPrefix: string;
  status: string;
  expiresAt?: string;
  lastUsedAt?: string;
  createdAt: string;
  revokedAt?: string;
}

export interface CreateApiKeyResponse {
  id: string;
  applicationId: string;
  keyPrefix: string;
  fullSecretKey: string;
  status: string;
  expiresAt?: string;
  createdAt: string;
}

export interface UsageRecord {
  id: string;
  requestId: string;
  apiId: string;
  apiName: string;
  applicationId: string;
  applicationName: string;
  apiKeyId?: string;
  statusCode: number;
  latencyMs: number;
  inputUnits: number;
  outputUnits: number;
  estimatedCost: number;
  cacheHit: boolean;
  rejected: boolean;
  rejectionReason?: string;
  createdAt: string;
}

export interface UsageSummary {
  totalRequests: number;
  totalCost: number;
  errorCount: number;
  errorRatePercentage: number;
  cacheHitCount: number;
  cacheHitPercentage: number;
  activeApisCount: number;
  openAnomaliesCount: number;
}

export interface CostBreakdown {
  apiId: string;
  apiName: string;
  requestCount: number;
  totalCost: number;
}

export interface Budget {
  id: string;
  apiId: string;
  apiName: string;
  periodType: string;
  limitAmount: number;
  currentSpend: number;
  percentageUsed: number;
  warningPercent: number;
  criticalPercent: number;
  blockingEnabled: boolean;
  status: 'NORMAL' | 'WARNING' | 'CRITICAL' | 'EXCEEDED';
  currentPeriodStart: string;
  updatedAt: string;
}

export interface Anomaly {
  id: string;
  apiId: string;
  apiName: string;
  type: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  observedValue: number;
  expectedValue: number;
  threshold: number;
  status: 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED';
  detectedAt: string;
  resolvedAt?: string;
  description: string;
}

export interface AuditLog {
  id: string;
  actorUserId?: string;
  action: string;
  resourceType: string;
  resourceId?: string;
  metadata?: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}
