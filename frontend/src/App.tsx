import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { AlertsPage } from "./pages/AlertsPage";
import { DashboardPage } from "./pages/DashboardPage";
import { GraphExplorerPage } from "./pages/GraphExplorerPage";
import { InvestigationDetailPage } from "./pages/InvestigationDetailPage";
import { SearchPage } from "./pages/SearchPage";
import { HypergraphExplorerPage } from "./features/hypergraph/HypergraphExplorerPage";

export function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/alerts" element={<AlertsPage />} />
        <Route path="/investigations/:alertId" element={<InvestigationDetailPage />} />
        <Route path="/graph" element={<GraphExplorerPage />} />
        <Route path="/hypergraph" element={<HypergraphExplorerPage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppShell>
  );
}
