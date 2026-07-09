package com.fraudgraph.investigator.hypergraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class HypergraphPatternDetector {
    public List<SuspiciousHypergraphPatternDTO> detect(HypergraphDTO hypergraph) {
        List<SuspiciousHypergraphPatternDTO> patterns = new ArrayList<>();
        patterns.addAll(sharedDevicePatterns(hypergraph));
        patterns.addAll(sharedMerchantBurstPatterns(hypergraph));
        patterns.addAll(multiEntitySimilarityPatterns(hypergraph));
        return patterns;
    }

    private List<SuspiciousHypergraphPatternDTO> sharedDevicePatterns(HypergraphDTO hypergraph) {
        Map<String, Set<String>> deviceToAccounts = new HashMap<>();
        Map<String, Set<String>> deviceToEdges = new HashMap<>();
        Map<String, HyperNodeDTO> nodes = indexNodes(hypergraph);

        for (HyperEdgeDTO edge : hypergraph.hyperedges()) {
            List<String> devices = edge.connectedNodeIds().stream().filter(id -> isType(nodes, id, "Device")).toList();
            List<String> accounts = edge.connectedNodeIds().stream().filter(id -> isType(nodes, id, "Account")).toList();
            for (String device : devices) {
                deviceToAccounts.computeIfAbsent(device, ignored -> new HashSet<>()).addAll(accounts);
                deviceToEdges.computeIfAbsent(device, ignored -> new HashSet<>()).add(edge.id());
            }
        }

        return deviceToAccounts.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 3)
                .map(entry -> new SuspiciousHypergraphPatternDTO(
                        "SHARED_DEVICE_PATTERN",
                        "HIGH",
                        prepend(entry.getKey(), entry.getValue()),
                        new ArrayList<>(deviceToEdges.getOrDefault(entry.getKey(), Set.of())),
                        "Multiple accounts participated in payment events through the same device.",
                        "Inspect device fingerprint, account ownership, and recent payment events."
                ))
                .toList();
    }

    private List<SuspiciousHypergraphPatternDTO> sharedMerchantBurstPatterns(HypergraphDTO hypergraph) {
        Map<String, Set<String>> merchantToAccounts = new HashMap<>();
        Map<String, Set<String>> merchantToEdges = new HashMap<>();
        Map<String, HyperNodeDTO> nodes = indexNodes(hypergraph);

        for (HyperEdgeDTO edge : hypergraph.hyperedges()) {
            List<String> merchants = edge.connectedNodeIds().stream().filter(id -> isType(nodes, id, "Merchant")).toList();
            List<String> accounts = edge.connectedNodeIds().stream().filter(id -> isType(nodes, id, "Account")).toList();
            for (String merchant : merchants) {
                merchantToAccounts.computeIfAbsent(merchant, ignored -> new HashSet<>()).addAll(accounts);
                merchantToEdges.computeIfAbsent(merchant, ignored -> new HashSet<>()).add(edge.id());
            }
        }

        return merchantToAccounts.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 5)
                .map(entry -> new SuspiciousHypergraphPatternDTO(
                        "SHARED_MERCHANT_BURST_PATTERN",
                        "MEDIUM",
                        prepend(entry.getKey(), entry.getValue()),
                        new ArrayList<>(merchantToEdges.getOrDefault(entry.getKey(), Set.of())),
                        "Many unrelated accounts participated in payment events with the same merchant.",
                        "Review merchant legitimacy and account funding sources."
                ))
                .toList();
    }

    private List<SuspiciousHypergraphPatternDTO> multiEntitySimilarityPatterns(HypergraphDTO hypergraph) {
        List<SuspiciousHypergraphPatternDTO> patterns = new ArrayList<>();
        for (int i = 0; i < hypergraph.hyperedges().size(); i++) {
            HyperEdgeDTO left = hypergraph.hyperedges().get(i);
            for (int j = i + 1; j < hypergraph.hyperedges().size(); j++) {
                HyperEdgeDTO right = hypergraph.hyperedges().get(j);
                Set<String> overlap = new HashSet<>(left.connectedNodeIds());
                overlap.retainAll(right.connectedNodeIds());
                if (overlap.size() >= 3) {
                    patterns.add(new SuspiciousHypergraphPatternDTO(
                            "MULTI_ENTITY_SIMILARITY_PATTERN",
                            "HIGH",
                            new ArrayList<>(overlap),
                            List.of(left.id(), right.id()),
                            "Payment events share multiple participating entities.",
                            "Compare event timing, device/IP reuse, and merchant behavior."
                    ));
                }
            }
        }
        return patterns.stream().limit(10).toList();
    }

    private Map<String, HyperNodeDTO> indexNodes(HypergraphDTO hypergraph) {
        Map<String, HyperNodeDTO> nodes = new HashMap<>();
        hypergraph.nodes().forEach(node -> nodes.put(node.id(), node));
        return nodes;
    }

    private boolean isType(Map<String, HyperNodeDTO> nodes, String nodeId, String type) {
        HyperNodeDTO node = nodes.get(nodeId);
        return node != null && type.equals(node.type());
    }

    private List<String> prepend(String first, Set<String> rest) {
        List<String> values = new ArrayList<>();
        values.add(first);
        values.addAll(rest);
        return values;
    }
}
