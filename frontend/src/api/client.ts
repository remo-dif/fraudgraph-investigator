import type {
  AlertDetail,
  AlertListItem,
  DashboardSummary,
  DetectionResult,
  GraphModel,
  HypergraphModel,
  SearchResult,
} from "../types/domain";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...init?.headers },
    ...init,
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `Request failed with ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export const api = {
  dashboard: () => request<DashboardSummary>("/api/dashboard/summary"),
  alerts: () => request<AlertListItem[]>("/api/alerts"),
  alert: (alertId: string) => request<AlertDetail>(`/api/alerts/${alertId}`),
  search: (query: string) => request<SearchResult[]>(`/api/search?q=${encodeURIComponent(query)}`),
  graph: (entityId: string, depth = 2) =>
    request<GraphModel>(`/api/graph/entity/${entityId}/neighborhood?depth=${depth}`),
  detections: () => request<DetectionResult[]>("/api/detections"),
  hypergraphCase: (caseId: string) => request<HypergraphModel>(`/api/hypergraph/cases/${caseId}`),
  hypergraphEntity: (entityId: string) => request<HypergraphModel>(`/api/hypergraph/entities/${entityId}/neighborhood`),
  hypergraphEvent: (eventId: string) => request<HypergraphModel>(`/api/hypergraph/events/${eventId}`),
  hypergraphPatterns: () => request<import("../types/domain").SuspiciousHypergraphPattern[]>("/api/hypergraph/patterns/suspicious-clusters"),
  createCase: (payload: { title: string; alertIds: string[]; recommendedActions?: string[] }) =>
    request("/api/cases", { method: "POST", body: JSON.stringify(payload) }),
};
