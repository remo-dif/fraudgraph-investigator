import { useMemo, useState } from "react";
import type { HyperEdge, HypergraphModel, HyperNode } from "../../../types/domain";

export function useHypergraphSelection(graph?: HypergraphModel) {
  const [selectedHyperedgeId, setSelectedHyperedgeId] = useState<string | null>(null);

  const selectedHyperedge = useMemo<HyperEdge | undefined>(
    () => graph?.hyperedges.find((edge) => edge.id === selectedHyperedgeId),
    [graph, selectedHyperedgeId],
  );

  const selectedNodes = useMemo<HyperNode[]>(
    () => graph?.nodes.filter((node) => selectedHyperedge?.connectedNodeIds.includes(node.id)) ?? [],
    [graph, selectedHyperedge],
  );

  return {
    selectedHyperedgeId,
    selectedHyperedge,
    selectedNodes,
    selectHyperedge: setSelectedHyperedgeId,
    clearSelection: () => setSelectedHyperedgeId(null),
  };
}
