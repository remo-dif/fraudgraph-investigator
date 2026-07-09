import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { EntityList } from "./EntityList";

describe("EntityList", () => {
  it("notifies when an entity is selected", async () => {
    const onSelect = vi.fn();
    render(
      <EntityList
        onSelect={onSelect}
        entities={[{ id: "acct-001", type: "ACCOUNT", label: "FG-42000001", riskScore: 82, subtitle: "Account" }]}
      />,
    );

    await userEvent.click(screen.getByRole("button", { name: /FG-42000001/i }));

    expect(onSelect).toHaveBeenCalledWith("acct-001");
  });
});
