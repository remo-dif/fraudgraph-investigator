export function RiskBadge({ score, severity }: { score?: number; severity?: string }) {
  const level = severity ?? (score == null ? "LOW" : score >= 85 ? "CRITICAL" : score >= 70 ? "HIGH" : score >= 50 ? "MEDIUM" : "LOW");
  return <span className={`risk-badge risk-${level.toLowerCase()}`}>{score ?? level}</span>;
}
