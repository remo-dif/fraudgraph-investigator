import type { NodeProps } from "@xyflow/react";
import type { HyperEdge as HyperEdgeModel } from "../../../types/domain";

type HyperEdgeData = HyperEdgeModel & { suspicious?: boolean };

export function HyperEdge({ data }: NodeProps) {
  const edge = data as unknown as HyperEdgeData;
  return (
    <div className={`hyper-edge-node ${edge.suspicious ? "hyper-edge-suspicious" : ""}`}>
      <span>{edge.type}</span>
      <strong>{edge.label}</strong>
      {edge.riskScore != null ? <small>Risk {edge.riskScore}</small> : null}
    </div>
  );
}
