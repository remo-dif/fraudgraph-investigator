import { AlertOctagon, CircleDollarSign, Network, ShieldAlert } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { RiskBadge } from "../components/RiskBadge";
import { StatCard } from "../components/StatCard";

export function DashboardPage() {
  const { data, isLoading, error } = useQuery({ queryKey: ["dashboard"], queryFn: api.dashboard });

  if (isLoading) return <LoadingState />;
  if (error || !data) return <ErrorState message="Unable to load dashboard summary." />;

  return (
    <>
      <PageHeader title="Fraud investigation command center" eyebrow="Operational overview" />
      <section className="stats-grid">
        <StatCard label="Open alerts" value={data.openAlerts} detail="Require analyst review" icon={<AlertOctagon size={18} />} />
        <StatCard label="Active cases" value={data.activeCases} detail="Open or triaged" icon={<ShieldAlert size={18} />} />
        <StatCard label="High-risk entities" value={data.highRiskEntities} detail="Risk score at least 75" icon={<Network size={18} />} />
        <StatCard
          label="Flagged exposure"
          value={Number(data.suspiciousAmount).toLocaleString(undefined, { style: "currency", currency: "USD" })}
          detail="Transaction value under alert"
          icon={<CircleDollarSign size={18} />}
        />
      </section>
      <section className="content-band">
        <div className="section-title">
          <h2>Detected typologies</h2>
          <Link to="/graph">Open graph explorer</Link>
        </div>
        <div className="pattern-grid">
          {data.patternSummaries.map((pattern) => (
            <article className="pattern-card" key={pattern.key}>
              <RiskBadge severity={pattern.severity} />
              <strong>{pattern.label}</strong>
              <span>{pattern.count} current matches</span>
            </article>
          ))}
        </div>
      </section>
    </>
  );
}
