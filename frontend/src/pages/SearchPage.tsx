import { Search } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router";
import { api } from "../api/client";
import { EntityList } from "../components/EntityList";
import { PageHeader } from "../components/PageHeader";

export function SearchPage() {
  const [query, setQuery] = useState("acct");
  const navigate = useNavigate();
  const { data = [], isFetching } = useQuery({
    queryKey: ["search", query],
    queryFn: () => api.search(query),
    enabled: query.trim().length >= 2,
  });

  return (
    <>
      <PageHeader title="Entity search" eyebrow="Customers, accounts, devices, alerts, and cases" />
      <section className="search-panel">
        <Search size={18} aria-hidden />
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search by name, id, account number, or alert title" />
      </section>
      <section className="content-band">
        <div className="section-title">
          <h2>Results</h2>
          <span>{isFetching ? "Searching" : `${data.length} matches`}</span>
        </div>
        <EntityList entities={data} onSelect={(id) => navigate(`/graph?entity=${encodeURIComponent(id)}`)} />
      </section>
    </>
  );
}
