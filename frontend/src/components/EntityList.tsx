import type { EntitySummary, SearchResult } from "../types/domain";
import { RiskBadge } from "./RiskBadge";

type Entity = EntitySummary | SearchResult;

export function EntityList({ entities, onSelect }: { entities: Entity[]; onSelect?: (id: string) => void }) {
  return (
    <div className="entity-list">
      {entities.map((entity) => (
        <button key={entity.id} className="entity-row" type="button" onClick={() => onSelect?.(entity.id)}>
          <span className="entity-type">{entity.type}</span>
          <span className="entity-main">
            <strong>{entity.label}</strong>
            <small>{entity.id}</small>
          </span>
          <RiskBadge score={entity.riskScore} />
        </button>
      ))}
    </div>
  );
}
