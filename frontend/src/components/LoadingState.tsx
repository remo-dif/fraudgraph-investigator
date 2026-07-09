export function LoadingState({ label = "Loading investigation data" }: { label?: string }) {
  return <div className="state-panel">{label}</div>;
}
