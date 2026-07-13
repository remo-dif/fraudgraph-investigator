import { BriefcaseBusiness, RefreshCw } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router";
import { api } from "../api/client";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { RiskBadge } from "../components/RiskBadge";

export function AlertsPage() {
  const queryClient = useQueryClient();
  const { data, isLoading, error } = useQuery({ queryKey: ["alerts"], queryFn: api.alerts });
  const createCase = useMutation({
    mutationFn: (alertId: string) =>
      api.createCase({
        title: `Investigation for ${alertId}`,
        alertIds: [alertId],
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["dashboard"] }),
  });

  if (isLoading) return <LoadingState />;
  if (error || !data) return <ErrorState message="Unable to load alerts." />;

  return (
    <>
      <PageHeader
        title="Alert queue"
        eyebrow="Prioritized review"
        actions={
          <button className="icon-button" type="button" onClick={() => queryClient.invalidateQueries({ queryKey: ["alerts"] })} title="Refresh alerts">
            <RefreshCw size={16} />
          </button>
        }
      />
      <section className="table-panel">
        <table>
          <thead>
            <tr>
              <th>Alert</th>
              <th>Entity</th>
              <th>Status</th>
              <th>Severity</th>
              <th>Risk</th>
              <th>Created</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {data.map((alert) => (
              <tr key={alert.id}>
                <td>
                  <Link to={`/investigations/${alert.id}`}>{alert.title}</Link>
                  <small>{alert.id}</small>
                </td>
                <td>{alert.entityLabel}</td>
                <td>{alert.status}</td>
                <td><RiskBadge severity={alert.severity} /></td>
                <td><RiskBadge score={alert.riskScore} /></td>
                <td>{new Date(alert.createdAt).toLocaleString()}</td>
                <td>
                  <button className="row-action" type="button" onClick={() => createCase.mutate(alert.id)} title="Create case">
                    <BriefcaseBusiness size={15} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  );
}
