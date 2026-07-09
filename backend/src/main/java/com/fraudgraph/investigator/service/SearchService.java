package com.fraudgraph.investigator.service;

import com.fraudgraph.investigator.dto.SearchResultDto;
import com.fraudgraph.investigator.repository.GraphQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final GraphQueryRepository graphQueryRepository;

    public SearchService(GraphQueryRepository graphQueryRepository) {
        this.graphQueryRepository = graphQueryRepository;
    }

    public List<SearchResultDto> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return graphQueryRepository.searchEntities(query.trim());
    }
}
