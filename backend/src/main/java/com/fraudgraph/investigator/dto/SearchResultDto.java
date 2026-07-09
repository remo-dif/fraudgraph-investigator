package com.fraudgraph.investigator.dto;

public record SearchResultDto(String id, String type, String label, Integer riskScore, String subtitle) {
}
