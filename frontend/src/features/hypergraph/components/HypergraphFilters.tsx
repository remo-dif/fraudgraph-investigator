export type HypergraphFilterState = {
  nodeTypes: string[];
  hyperedgeTypes: string[];
  minRisk: number;
};

export function HypergraphFilters({
  nodeTypes,
  hyperedgeTypes,
  value,
  onChange,
}: {
  nodeTypes: string[];
  hyperedgeTypes: string[];
  value: HypergraphFilterState;
  onChange: (next: HypergraphFilterState) => void;
}) {
  function toggle(kind: "nodeTypes" | "hyperedgeTypes", item: string) {
    const selected = value[kind];
    onChange({
      ...value,
      [kind]: selected.includes(item) ? selected.filter((entry) => entry !== item) : [...selected, item],
    });
  }

  return (
    <section className="hyper-filters">
      <h2>Filters</h2>
      <label>
        Minimum risk
        <input
          type="range"
          min="0"
          max="100"
          value={value.minRisk}
          onChange={(event) => onChange({ ...value, minRisk: Number(event.target.value) })}
        />
        <span>{value.minRisk}</span>
      </label>
      <h3>Node types</h3>
      {nodeTypes.map((type) => (
        <label className="check-row" key={type}>
          <input type="checkbox" checked={value.nodeTypes.includes(type)} onChange={() => toggle("nodeTypes", type)} />
          {type}
        </label>
      ))}
      <h3>Hyperedge types</h3>
      {hyperedgeTypes.map((type) => (
        <label className="check-row" key={type}>
          <input type="checkbox" checked={value.hyperedgeTypes.includes(type)} onChange={() => toggle("hyperedgeTypes", type)} />
          {type}
        </label>
      ))}
    </section>
  );
}
