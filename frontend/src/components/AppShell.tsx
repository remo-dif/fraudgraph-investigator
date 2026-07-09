import { AlertTriangle, Gauge, GitFork, Network, Search } from "lucide-react";
import { NavLink } from "react-router-dom";
import type { ReactNode } from "react";

const navItems = [
  { to: "/", label: "Dashboard", icon: Gauge },
  { to: "/alerts", label: "Alerts", icon: AlertTriangle },
  { to: "/graph", label: "Graph", icon: GitFork },
  { to: "/hypergraph", label: "Hypergraph", icon: Network },
  { to: "/search", label: "Search", icon: Search },
];

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">FG</span>
          <div>
            <strong>FraudGraph</strong>
            <small>Investigator</small>
          </div>
        </div>
        <nav className="nav-list" aria-label="Primary navigation">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === "/"} className="nav-link">
              <item.icon size={18} aria-hidden />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="main-panel">{children}</main>
    </div>
  );
}
