import { useQuery } from "@tanstack/react-query";
import { api } from "../../../api/client";

export function useHypergraphData(scope: "case" | "entity" | "event", id: string) {
  return useQuery({
    queryKey: ["hypergraph", scope, id],
    queryFn: () => {
      if (scope === "case") return api.hypergraphCase(id);
      if (scope === "event") return api.hypergraphEvent(id);
      return api.hypergraphEntity(id);
    },
    enabled: Boolean(id.trim()),
  });
}
