package com.fraudgraph.investigator.controller;

import com.fraudgraph.investigator.dto.GraphDto;
import com.fraudgraph.investigator.service.GraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
public class GraphController {
    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/entity/{entityId}/neighborhood")
    public GraphDto neighborhood(@PathVariable String entityId, @RequestParam(defaultValue = "2") int depth) {
        return graphService.neighborhood(entityId, depth);
    }
}
