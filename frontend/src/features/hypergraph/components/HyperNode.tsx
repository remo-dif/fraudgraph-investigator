import type { NodeProps } from "@xyflow/react";
import type { HyperNode as HyperNodeModel } from "../../../types/domain";

export function HyperNode({ data }: NodeProps) {
  const node = data as unknown as HyperNodeModel;
  return (
    <div className="hyper-node">
      <span>{node.type}</span>
      <strong>{node.label}</strong>
      {node.riskScore != null ? <small>Risk {node.riskScore}</small> : null}
    </div>
  );
}
