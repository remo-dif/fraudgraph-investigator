export type DashboardSummary = {
  openAlerts: number;
  activeCases: number;
  highRiskEntities: number;
  suspiciousAmount: number;
  patternSummaries: PatternSummary[];
};

export type PatternSummary = {
  key: string;
  label: string;
  count: number;
  severity: string;
};

export type AlertListItem = {
  id: string;
  title: string;
  status: string;
  severity: string;
  riskScore: number;
  entityId: string;
  entityLabel: string;
  createdAt: string;
};

export type AlertDetail = {
  id: string;
  title: string;
  status: string;
  severity: string;
  riskScore: number;
  description: string;
  createdAt: string;
  riskReasons: string[];
  timeline: TimelineEvent[];
  relatedEntities: EntitySummary[];
  graph: GraphModel;
  recommendedActions: string[];
};

export type TimelineEvent = {
  occurredAt: string;
  type: string;
  description: string;
  amount: number;
  currency: string;
};

export type EntitySummary = {
  id: string;
  type: string;
  label: string;
  riskScore?: number;
  properties: Record<string, unknown>;
};

export type SearchResult = {
  id: string;
  type: string;
  label: string;
  riskScore?: number;
  subtitle: string;
};

export type DetectionResult = {
  pattern: string;
  severity: string;
  summary: string;
  entityIds: string[];
  reasons: string[];
};

export type GraphModel = {
  nodes: GraphNode[];
  edges: GraphEdge[];
};

export type GraphNode = {
  id: string;
  type: string;
  label: string;
  riskScore?: number;
  properties: Record<string, unknown>;
};

export type GraphEdge = {
  id: string;
  source: string;
  target: string;
  type: string;
  properties: Record<string, unknown>;
};

export type HyperNode = {
  id: string;
  type: string;
  label: string;
  riskScore?: number;
  metadata: Record<string, unknown>;
};

export type HyperEdge = {
  id: string;
  type: string;
  label: string;
  riskScore?: number;
  connectedNodeIds: string[];
  timestamp?: string;
  metadata: Record<string, unknown>;
};

export type SuspiciousHypergraphPattern = {
  patternType: string;
  severity: string;
  involvedNodeIds: string[];
  involvedHyperedgeIds: string[];
  explanation: string;
  recommendedAction: string;
};

export type HypergraphModel = {
  nodes: HyperNode[];
  hyperedges: HyperEdge[];
  suspiciousPatterns: SuspiciousHypergraphPattern[];
};
