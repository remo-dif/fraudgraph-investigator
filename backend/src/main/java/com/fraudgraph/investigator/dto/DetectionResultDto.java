package com.fraudgraph.investigator.dto;

import java.util.List;

public record DetectionResultDto(
        String pattern,
        String severity,
        String summary,
        List<String> entityIds,
        List<String> reasons
) {
}
