package com.fraudgraph.investigator.controller;

import com.fraudgraph.investigator.dto.SearchResultDto;
import com.fraudgraph.investigator.service.SearchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.context.annotation.Profile("demo")
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<SearchResultDto> search(@RequestParam String q) {
        return searchService.search(q);
    }
}
