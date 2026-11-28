package com.fraudgraph.investigator.controller;

import com.fraudgraph.investigator.dto.CaseDto;
import com.fraudgraph.investigator.dto.CreateCaseRequest;
import com.fraudgraph.investigator.dto.UpdateCaseStatusRequest;
import com.fraudgraph.investigator.service.CaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.context.annotation.Profile("demo")
@RestController
@RequestMapping("/api/cases")
public class CaseController {
    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CaseDto create(@Valid @RequestBody CreateCaseRequest request) {
        return caseService.create(request);
    }

    @PatchMapping("/{caseId}/status")
    public CaseDto updateStatus(@PathVariable String caseId, @Valid @RequestBody UpdateCaseStatusRequest request) {
        return caseService.updateStatus(caseId, request.status());
    }
}
