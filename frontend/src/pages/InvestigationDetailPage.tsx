import { ArrowLeft, BriefcaseBusiness } from "lucide-react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";
import { EntityList } from "../components/EntityList";
import { ErrorState } from "../components/ErrorState";
import { GraphCanvas } from "../components/GraphCanvas";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { RiskBadge } from "../components/RiskBadge";
import { Timeline } from "../components/Timeline";

export function InvestigationDetailPage() {
  const { alertId = "" } = useParams();
  const { data, isLoading, error } = useQuery({ queryKey: ["alert", alertId], queryFn: () => api.alert(alertId), enabled: Boolean(alertId) });
  const createCase = useMutation({
    mutationFn: () =>
      api.createCase({
        title: data ? `Case review: ${data.title}` : "New investigation case",
        alertIds: [alertId],
        recommendedActions: data?.recommendedActions,
      }),
  });

  if (isLoading) return <LoadingState />;
  if (error || !data) return <ErrorState message="Unable to load investigation detail." />;

  return (
    <>
      <PageHeader
        title={data.title}
        eyebrow="Investigation detail"
        actions={
          <>
            <Link className="secondary-link" to="/alerts"><ArrowLeft size={15} /> Alerts</Link>
            <button className="primary-button" type="button" onClick={() => createCase.mutate()}>
              <BriefcaseBusiness size={16} /> Create case
            </button>
          </>
        }
      />
      <section className="detail-grid">
        <article className="summary-panel">
          <div className="section-title">
            <h2>Case summary</h2>
            <RiskBadge score={data.riskScore} />
          </div>
          <p>{data.description}</p>
          <h3>Risk reasons</h3>
          <ul className="reason-list">
            {data.riskReasons.map((reason) => <li key={reason}>{reason}</li>)}
          </ul>
        </article>
        <article className="summary-panel">
          <h2>Recommended next actions</h2>
          <ol className="action-list">
            {data.recommendedActions.map((action) => <li key={action}>{action}</li>)}
          </ol>
        </article>
      </section>
      <section className="content-band">
        <h2>Transaction timeline</h2>
        <Timeline events={data.timeline} />
      </section>
      <section className="detail-grid">
        <article className="summary-panel">
          <h2>Related entities</h2>
          <EntityList entities={data.relatedEntities} />
        </article>
        <article className="summary-panel graph-panel">
          <h2>Graph neighborhood</h2>
          <GraphCanvas graph={data.graph} />
        </article>
      </section>
    </>
  );
}
