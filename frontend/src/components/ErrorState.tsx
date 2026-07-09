export function ErrorState({ message }: { message: string }) {
  return <div className="state-panel error-state">{message}</div>;
}
