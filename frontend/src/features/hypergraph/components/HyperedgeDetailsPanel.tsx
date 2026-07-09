import type { HyperEdge, HyperNode, SuspiciousHypergraphPattern } from "../../../types/domain";

export function HyperedgeDetailsPanel({
  edge,
  nodes,
  patterns,
}: {
  edge?: HyperEdge;
  nodes: HyperNode[];
  patterns: SuspiciousHypergraphPattern[];
}) {
  if (!edge) {
    return <aside className="hyper-details">Select an event bubble to inspect participants and suspicious context.</aside>;
  }

  const relatedPatterns = patterns.filter((pattern) => pattern.involvedHyperedgeIds.includes(edge.id));

  return (
    <aside className="hyper-details">
      <h2>{edge.label}</h2>
      <p className="muted">{edge.type} · Risk {edge.riskScore ?? "n/a"}</p>
      <h3>Participants</h3>
      <div className="hyper-participants">
        {nodes.map((node) => (
          <span key={node.id}>{node.type}: {node.label}</span>
        ))}
      </div>
      <h3>Why suspicious</h3>
      {relatedPatterns.length === 0 ? <p className="muted">No suspicious pattern is attached to this event.</p> : null}
      {relatedPatterns.map((pattern) => (
        <article key={`${pattern.patternType}-${pattern.involvedHyperedgeIds.join("-")}`} className="pattern-note">
          <strong>{pattern.patternType}</strong>
          <p>{pattern.explanation}</p>
          <small>{pattern.recommendedAction}</small>
        </article>
      ))}
    </aside>
  );
}
