package com.schemascope.service.impl;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.service.DefenseShowcaseBuilder;
import com.schemascope.service.DefenseShowcaseService;
import com.schemascope.service.PrReviewService;
import org.springframework.stereotype.Service;

@Service
public class MockDefenseShowcaseService implements DefenseShowcaseService {

    private final PrReviewService prReviewService;
    private final DefenseShowcaseBuilder defenseShowcaseBuilder;

    public MockDefenseShowcaseService(PrReviewService prReviewService,
                                      DefenseShowcaseBuilder defenseShowcaseBuilder) {
        this.prReviewService = prReviewService;
        this.defenseShowcaseBuilder = defenseShowcaseBuilder;
    }

    @Override
    public DefenseShowcasePack buildShowcase(AnalysisRequest request) {
        PrReviewReport report = prReviewService.review(request);
        return defenseShowcaseBuilder.build(request, report);
    }
}