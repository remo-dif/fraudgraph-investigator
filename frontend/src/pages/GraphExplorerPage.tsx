import { RotateCcw, Search, UnfoldHorizontal } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { api } from "../api/client";
import { EntityList } from "../components/EntityList";
import { ErrorState } from "../components/ErrorState";
import { GraphCanvas } from "../components/GraphCanvas";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";

export function GraphExplorerPage() {
  const [params, setParams] = useSearchParams();
  const [query, setQuery] = useState(params.get("entity") ?? "acct-003");
  const [entityId, setEntityId] = useState(params.get("entity") ?? "acct-003");
  const [selectedTypes, setSelectedTypes] = useState<string[]>([]);
  const [selectedRelations, setSelectedRelations] = useState<string[]>([]);
  const { data: searchResults = [] } = useQuery({
    queryKey: ["search", query],
    queryFn: () => api.search(query),
    enabled: query.trim().length >= 2,
  });
  const { data: graph, isLoading, error, refetch } = useQuery({
    queryKey: ["graph", entityId],
    queryFn: () => api.graph(entityId, 2),
    enabled: Boolean(entityId),
  });

  const nodeTypes = useMemo(() => Array.from(new Set(graph?.nodes.map((node) => node.type) ?? [])).sort(), [graph]);
  const relationTypes = useMemo(() => Array.from(new Set(graph?.edges.map((edge) => edge.type) ?? [])).sort(), [graph]);

  function toggle(value: string, selected: string[], setter: (next: string[]) => void) {
    setter(selected.includes(value) ? selected.filter((item) => item !== value) : [...selected, value]);
  }

  function focusEntity(id: string) {
    setEntityId(id);
    setParams({ entity: id });
  }

  return (
    <>
      <PageHeader title="Graph explorer" eyebrow="Neighborhood expansion and filtering" />
      <section className="explorer-layout">
        <aside className="explorer-sidebar">
          <div className="search-panel compact">
            <Search size={16} aria-hidden />
            <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Find entity" />
          </div>
          <EntityList entities={searchResults.slice(0, 6)} onSelect={focusEntity} />
          <div className="filter-block">
            <h3>Node types</h3>
            {nodeTypes.map((type) => (
              <label key={type} className="check-row">
                <input type="checkbox" checked={selectedTypes.includes(type)} onChange={() => toggle(type, selectedTypes, setSelectedTypes)} />
                {type}
              </label>
            ))}
          </div>
          <div className="filter-block">
            <h3>Relationships</h3>
            {relationTypes.map((type) => (
              <label key={type} className="check-row">
                <input type="checkbox" checked={selectedRelations.includes(type)} onChange={() => toggle(type, selectedRelations, setSelectedRelations)} />
                {type}
              </label>
            ))}
          </div>
          <div className="button-row">
            <button className="secondary-button" type="button" onClick={() => refetch()}>
              <UnfoldHorizontal size={15} /> Expand
            </button>
            <button className="secondary-button" type="button" onClick={() => { setSelectedTypes([]); setSelectedRelations([]); }}>
              <RotateCcw size={15} /> Reset
            </button>
          </div>
        </aside>
        <section className="graph-workspace">
          {isLoading ? <LoadingState label="Loading graph neighborhood" /> : null}
          {error ? <ErrorState message="Unable to load graph neighborhood." /> : null}
          {graph ? (
            <GraphCanvas
              graph={graph}
              selectedTypes={selectedTypes}
              selectedRelations={selectedRelations}
              onNodeClick={focusEntity}
            />
          ) : null}
        </section>
      </section>
    </>
  );
}
