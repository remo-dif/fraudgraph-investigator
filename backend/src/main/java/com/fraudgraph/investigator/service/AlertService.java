package com.fraudgraph.investigator.service;

import com.fraudgraph.investigator.dto.AlertDetailDto;
import com.fraudgraph.investigator.dto.AlertListItemDto;
import com.fraudgraph.investigator.exception.NotFoundException;
import com.fraudgraph.investigator.repository.GraphQueryRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@org.springframework.context.annotation.Profile("demo")
@Service
public class AlertService {
    private final GraphQueryRepository graphQueryRepository;

    public AlertService(GraphQueryRepository graphQueryRepository) {
        this.graphQueryRepository = graphQueryRepository;
    }

    public List<AlertListItemDto> listAlerts() {
        return graphQueryRepository.listAlerts();
    }

    public AlertDetailDto getAlert(String alertId) {
        try {
            return graphQueryRepository.getAlertDetail(alertId);
        } catch (NoSuchElementException exception) {
            throw new NotFoundException("Alert %s was not found".formatted(alertId));
        }
    }
}
