package com.schemascope.presentation;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.view.ReviewPageView;
import com.schemascope.domain.view.ShowcaseDashboardView;
import com.schemascope.service.DefenseShowcaseService;
import com.schemascope.service.PresentationViewAssembler;
import com.schemascope.service.PrReviewService;

public class PresentationAssetAssembler {

    private final PrReviewService prReviewService;
    private final DefenseShowcaseService defenseShowcaseService;
    private final PresentationViewAssembler presentationViewAssembler;

    public PresentationAssetAssembler(PrReviewService prReviewService,
                                      DefenseShowcaseService defenseShowcaseService,
                                      PresentationViewAssembler presentationViewAssembler) {
        this.prReviewService = prReviewService;
        this.defenseShowcaseService = defenseShowcaseService;
        this.presentationViewAssembler = presentationViewAssembler;
    }

    public ReviewPageView buildReviewPage(AnalysisRequest request) {
        PrReviewReport report = prReviewService.review(request);
        return presentationViewAssembler.toReviewPageView(report);
    }

    public ShowcaseDashboardView buildShowcaseDashboard(AnalysisRequest request) {
        DefenseShowcasePack pack = defenseShowcaseService.buildShowcase(request);
        return presentationViewAssembler.toShowcaseDashboardView(pack);
    }
}