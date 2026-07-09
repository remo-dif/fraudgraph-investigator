package com.fraudgraph.investigator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateCaseRequest(
        @NotBlank String title,
        @NotEmpty List<String> alertIds,
        List<String> recommendedActions
) {
}
