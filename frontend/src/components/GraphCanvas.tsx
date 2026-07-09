import { Background, Controls, MiniMap, ReactFlow, type Edge, type Node } from "@xyflow/react";
import { useMemo } from "react";
import type { GraphModel } from "../types/domain";

const nodeColors: Record<string, string> = {
  CUSTOMER: "#2563eb",
  ACCOUNT: "#0f766e",
  CARD: "#7c3aed",
  DEVICE: "#b45309",
  IP_ADDRESS: "#64748b",
  MERCHANT: "#be123c",
  TRANSACTION: "#0891b2",
  ALERT: "#dc2626",
  CASE: "#334155",
};

export function GraphCanvas({
  graph,
  selectedTypes,
  selectedRelations,
  onNodeClick,
}: {
  graph: GraphModel;
  selectedTypes?: string[];
  selectedRelations?: string[];
  onNodeClick?: (id: string) => void;
}) {
  const { nodes, edges } = useMemo(() => {
    const typeSet = new Set(selectedTypes);
    const relSet = new Set(selectedRelations);
    const visibleGraphNodes = graph.nodes.filter((node) => typeSet.size === 0 || typeSet.has(node.type));
    const visibleIds = new Set(visibleGraphNodes.map((node) => node.id));
    const flowNodes: Node[] = visibleGraphNodes.map((node, index) => ({
      id: node.id,
      position: {
        x: (index % 5) * 210,
        y: Math.floor(index / 5) * 130,
      },
      data: { label: `${node.label}\n${node.type}` },
      style: {
        border: `2px solid ${nodeColors[node.type] ?? "#475569"}`,
        background: "#ffffff",
        color: "#0f172a",
        width: 172,
        minHeight: 58,
        borderRadius: 6,
        fontSize: 12,
        whiteSpace: "pre-line",
      },
    }));
    const flowEdges: Edge[] = graph.edges
      .filter((edge) => visibleIds.has(edge.source) && visibleIds.has(edge.target))
      .filter((edge) => relSet.size === 0 || relSet.has(edge.type))
      .map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        label: edge.type,
        animated: edge.type === "FLAGS" || edge.type === "TRANSFERRED_TO",
      }));
    return { nodes: flowNodes, edges: flowEdges };
  }, [graph, selectedRelations, selectedTypes]);

  return (
    <div className="graph-canvas">
      <ReactFlow nodes={nodes} edges={edges} fitView onNodeClick={(_, node) => onNodeClick?.(node.id)}>
        <Background />
        <MiniMap />
        <Controls />
      </ReactFlow>
    </div>
  );
}
