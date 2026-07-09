package com.fraudgraph.investigator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphQueryRepositoryCypherTest {
    @Test
    void containsRequiredCypherPatterns() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/fraudgraph/investigator/repository/GraphQueryRepository.java"));

        assertThat(source).contains("LOGGED_IN_FROM", "TRANSFERRED_TO", "SENT_TO");
        assertThat(source).contains("sharedDevicePatterns", "circularTransferPatterns", "highRiskMerchantPatterns");
        assertThat(source).contains("connectedAccountPatterns", "suspiciousClusterPatterns");
    }
}
