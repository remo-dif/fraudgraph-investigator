import { EyeOff, Search } from "lucide-react";
import { useMemo, useState } from "react";
import { ErrorState } from "../../components/ErrorState";
import { LoadingState } from "../../components/LoadingState";
import { PageHeader } from "../../components/PageHeader";
import type { HypergraphModel } from "../../types/domain";
import { HyperedgeDetailsPanel } from "./components/HyperedgeDetailsPanel";
import { HypergraphCanvas } from "./components/HypergraphCanvas";
import { HypergraphFilters, type HypergraphFilterState } from "./components/HypergraphFilters";
import { HypergraphLegend } from "./components/HypergraphLegend";
import { useHypergraphData } from "./hooks/useHypergraphData";
import { useHypergraphSelection } from "./hooks/useHypergraphSelection";

export function HypergraphExplorerPage() {
  const [scope, setScope] = useState<"case" | "entity" | "event">("case");
  const [id, setId] = useState("case-seed-001");
  const [collapsedHyperedges, setCollapsedHyperedges] = useState<string[]>([]);
  const [filters, setFilters] = useState<HypergraphFilterState>({ nodeTypes: [], hyperedgeTypes: [], minRisk: 0 });
  const { data, isLoading, error } = useHypergraphData(scope, id);
  const filteredGraph = useMemo(() => (data ? filterHypergraph(data, filters) : undefined), [data, filters]);
  const suspiciousHyperedgeIds = useMemo(
    () => data?.suspiciousPatterns.flatMap((pattern) => pattern.involvedHyperedgeIds) ?? [],
    [data],
  );
  const suspiciousPatterns = data?.suspiciousPatterns ?? [];
  const selection = useHypergraphSelection(filteredGraph);

  const nodeTypes = useMemo(() => Array.from(new Set(data?.nodes.map((node) => node.type) ?? [])).sort(), [data]);
  const hyperedgeTypes = useMemo(() => Array.from(new Set(data?.hyperedges.map((edge) => edge.type) ?? [])).sort(), [data]);

  return (
    <>
      <PageHeader title="Hypergraph Explorer" eyebrow="Event-centric investigation workspace" />
      <section className="hypergraph-toolbar">
        <label>
          Scope
          <select value={scope} onChange={(event) => setScope(event.target.value as "case" | "entity" | "event")}>
            <option value="case">Case</option>
            <option value="entity">Entity</option>
            <option value="event">Event</option>
          </select>
        </label>
        <label className="hypergraph-id-input">
          <Search size={16} />
          <input value={id} onChange={(event) => setId(event.target.value)} placeholder="case-seed-001, acct-003, payment-event-txn-0001" />
        </label>
      </section>
      {isLoading ? <LoadingState label="Loading hypergraph events" /> : null}
      {error ? <ErrorState message="Unable to load hypergraph data." /> : null}
      {filteredGraph ? (
        <section className="hypergraph-layout">
          <aside className="hypergraph-left-panel">
            <HypergraphLegend />
            <HypergraphFilters nodeTypes={nodeTypes} hyperedgeTypes={hyperedgeTypes} value={filters} onChange={setFilters} />
            <section className="hyper-filters">
              <h2>Event groups</h2>
              {filteredGraph.hyperedges.map((edge) => (
                <button
                  className="collapse-event-row"
                  type="button"
                  key={edge.id}
                  onClick={() =>
                    setCollapsedHyperedges((current) =>
                      current.includes(edge.id) ? current.filter((item) => item !== edge.id) : [...current, edge.id],
                    )
                  }
                >
                  <EyeOff size={14} />
                  {edge.label}
                </button>
              ))}
            </section>
          </aside>
          <main className="hypergraph-main-panel">
            <HypergraphCanvas
              graph={filteredGraph}
              hiddenHyperedgeIds={collapsedHyperedges}
              suspiciousHyperedgeIds={suspiciousHyperedgeIds}
              onSelectHyperedge={selection.selectHyperedge}
            />
          </main>
          <HyperedgeDetailsPanel
            edge={selection.selectedHyperedge}
            nodes={selection.selectedNodes}
            patterns={suspiciousPatterns}
          />
        </section>
      ) : null}
    </>
  );
}

export function filterHypergraph(graph: HypergraphModel, filters: HypergraphFilterState): HypergraphModel {
  const nodeTypes = new Set(filters.nodeTypes);
  const hyperedgeTypes = new Set(filters.hyperedgeTypes);
  const nodes = graph.nodes.filter((node) => nodeTypes.size === 0 || nodeTypes.has(node.type));
  const visibleNodeIds = new Set(nodes.map((node) => node.id));
  const hyperedges = graph.hyperedges
    .filter((edge) => hyperedgeTypes.size === 0 || hyperedgeTypes.has(edge.type))
    .filter((edge) => (edge.riskScore ?? 0) >= filters.minRisk)
    .filter((edge) => edge.connectedNodeIds.some((nodeId) => visibleNodeIds.has(nodeId)))
    .map((edge) => ({
      ...edge,
      connectedNodeIds: edge.connectedNodeIds.filter((nodeId) => visibleNodeIds.has(nodeId)),
    }));
  return { nodes, hyperedges, suspiciousPatterns: graph.suspiciousPatterns };
}
