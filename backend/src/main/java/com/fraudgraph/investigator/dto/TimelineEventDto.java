package com.fraudgraph.investigator.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TimelineEventDto(
        OffsetDateTime occurredAt,
        String type,
        String description,
        BigDecimal amount,
        String currency
) {
}
