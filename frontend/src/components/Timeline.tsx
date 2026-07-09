import type { TimelineEvent } from "../types/domain";

export function Timeline({ events }: { events: TimelineEvent[] }) {
  return (
    <div className="timeline">
      {events.map((event) => (
        <article key={`${event.occurredAt}-${event.description}`} className="timeline-event">
          <time>{new Date(event.occurredAt).toLocaleString()}</time>
          <div>
            <strong>{event.type}</strong>
            <p>{event.description}</p>
          </div>
          <span>
            {event.amount.toLocaleString(undefined, { style: "currency", currency: event.currency })}
          </span>
        </article>
      ))}
    </div>
  );
}
