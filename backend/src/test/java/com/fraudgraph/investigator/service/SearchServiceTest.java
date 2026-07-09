package com.fraudgraph.investigator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fraudgraph.investigator.repository.GraphQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock
    private GraphQueryRepository graphQueryRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void returnsEmptyResultForBlankSearch() {
        assertThat(searchService.search("   ")).isEmpty();
        verifyNoInteractions(graphQueryRepository);
    }
}
