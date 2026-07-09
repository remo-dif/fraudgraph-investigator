package com.fraudgraph.investigator.dto;

import com.fraudgraph.investigator.domain.CaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCaseStatusRequest(@NotNull CaseStatus status) {
}
