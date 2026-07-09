import { describe, expect, it } from "vitest";
import { filterHypergraph } from "./HypergraphExplorerPage";
import type { HypergraphModel } from "../../types/domain";

describe("filterHypergraph", () => {
  it("filters hyperedges by risk and node type", () => {
    const graph: HypergraphModel = {
      nodes: [
        { id: "acct-001", type: "Account", label: "Account", riskScore: 80, metadata: {} },
        { id: "dev-001", type: "Device", label: "Device", riskScore: 90, metadata: {} },
      ],
      hyperedges: [
        { id: "event-001", type: "PaymentEvent", label: "Payment", riskScore: 85, connectedNodeIds: ["acct-001", "dev-001"], metadata: {} },
        { id: "event-002", type: "LoginEvent", label: "Login", riskScore: 25, connectedNodeIds: ["dev-001"], metadata: {} },
      ],
      suspiciousPatterns: [],
    };

    const filtered = filterHypergraph(graph, { nodeTypes: ["Account"], hyperedgeTypes: [], minRisk: 70 });

    expect(filtered.nodes).toHaveLength(1);
    expect(filtered.hyperedges).toHaveLength(1);
    expect(filtered.hyperedges[0].connectedNodeIds).toEqual(["acct-001"]);
  });
});
