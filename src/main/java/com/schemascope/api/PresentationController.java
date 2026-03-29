package com.schemascope.api;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.view.ReviewPageView;
import com.schemascope.domain.view.ShowcaseDashboardView;
import com.schemascope.service.DefenseShowcaseService;
import com.schemascope.service.PresentationViewAssembler;
import com.schemascope.service.PrReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/view")
public class PresentationController {

    private final PrReviewService prReviewService;
    private final DefenseShowcaseService defenseShowcaseService;
    private final PresentationViewAssembler presentationViewAssembler;

    public PresentationController(PrReviewService prReviewService,
                                  DefenseShowcaseService defenseShowcaseService,
                                  PresentationViewAssembler presentationViewAssembler) {
        this.prReviewService = prReviewService;
        this.defenseShowcaseService = defenseShowcaseService;
        this.presentationViewAssembler = presentationViewAssembler;
    }

    @PostMapping("/review-page")
    public ReviewPageView buildReviewPage(@RequestBody AnalysisRequest request) {
        PrReviewReport report = prReviewService.review(request);
        return presentationViewAssembler.toReviewPageView(report);
    }

    @PostMapping("/showcase-dashboard")
    public ShowcaseDashboardView buildShowcaseDashboard(@RequestBody AnalysisRequest request) {
        DefenseShowcasePack pack = defenseShowcaseService.buildShowcase(request);
        return presentationViewAssembler.toShowcaseDashboardView(pack);
    }
}