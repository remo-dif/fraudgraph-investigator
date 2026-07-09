import type { ReactNode } from "react";

export function StatCard({ label, value, detail, icon }: { label: string; value: ReactNode; detail?: string; icon?: ReactNode }) {
  return (
    <section className="stat-card">
      <div className="stat-top">
        <span>{label}</span>
        {icon}
      </div>
      <strong>{value}</strong>
      {detail ? <small>{detail}</small> : null}
    </section>
  );
}
