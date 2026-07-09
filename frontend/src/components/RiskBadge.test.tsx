import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { RiskBadge } from "./RiskBadge";

describe("RiskBadge", () => {
  it("renders critical styling for high scores", () => {
    render(<RiskBadge score={92} />);

    const badge = screen.getByText("92");
    expect(badge).toHaveClass("risk-critical");
  });
});
