import { useMemo } from "react";
import type { Edge, Node } from "@xyflow/react";
import type { HypergraphModel } from "../../../types/domain";

export function useHypergraphLayout(
  graph: HypergraphModel,
  hiddenHyperedgeIds: string[],
  suspiciousHyperedgeIds: string[],
) {
  return useMemo(() => {
    const hidden = new Set(hiddenHyperedgeIds);
    const suspicious = new Set(suspiciousHyperedgeIds);
    const entityNodes: Node[] = graph.nodes.map((node, index) => ({
      id: node.id,
      type: "hyperNode",
      position: {
        x: (index % 5) * 210,
        y: Math.floor(index / 5) * 150,
      },
      data: node,
    }));
    const eventNodes: Node[] = graph.hyperedges
      .filter((edge) => !hidden.has(edge.id))
      .map((edge, index) => ({
        id: edge.id,
        type: "hyperEdge",
        position: {
          x: 105 + (index % 4) * 250,
          y: 90 + Math.floor(index / 4) * 180,
        },
        data: { ...edge, suspicious: suspicious.has(edge.id) },
      }));
    const visibleEventIds = new Set(eventNodes.map((node) => node.id));
    const flowEdges: Edge[] = graph.hyperedges
      .filter((edge) => visibleEventIds.has(edge.id))
      .flatMap((edge) =>
        edge.connectedNodeIds.map((nodeId) => ({
          id: `${edge.id}-${nodeId}`,
          source: edge.id,
          target: nodeId,
          animated: suspicious.has(edge.id),
        })),
      );
    return { nodes: [...eventNodes, ...entityNodes], edges: flowEdges };
  }, [graph, hiddenHyperedgeIds, suspiciousHyperedgeIds]);
}
