import { Background, Controls, ReactFlow } from "@xyflow/react";
import { useMemo } from "react";
import type { HypergraphModel } from "../../../types/domain";
import { HyperEdge } from "./HyperEdge";
import { HyperNode } from "./HyperNode";
import { useHypergraphLayout } from "../hooks/useHypergraphLayout";

const nodeTypes = {
  hyperNode: HyperNode,
  hyperEdge: HyperEdge,
};

export function HypergraphCanvas({
  graph,
  hiddenHyperedgeIds,
  suspiciousHyperedgeIds,
  onSelectHyperedge,
}: {
  graph: HypergraphModel;
  hiddenHyperedgeIds: string[];
  suspiciousHyperedgeIds: string[];
  onSelectHyperedge: (id: string) => void;
}) {
  const layout = useHypergraphLayout(graph, hiddenHyperedgeIds, suspiciousHyperedgeIds);
  const eventIds = useMemo(() => new Set(graph.hyperedges.map((edge) => edge.id)), [graph.hyperedges]);

  return (
    <div className="hypergraph-canvas">
      <ReactFlow
        nodes={layout.nodes}
        edges={layout.edges}
        nodeTypes={nodeTypes}
        fitView
        onNodeClick={(_, node) => {
          if (eventIds.has(node.id)) onSelectHyperedge(node.id);
        }}
      >
        <Background />
        <Controls />
      </ReactFlow>
    </div>
  );
}
