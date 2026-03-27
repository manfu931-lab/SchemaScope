package com.schemascope.api;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.service.PrReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final PrReviewService prReviewService;

    public ReviewController(PrReviewService prReviewService) {
        this.prReviewService = prReviewService;
    }

    @PostMapping("/pr")
    public PrReviewReport reviewPullRequest(@RequestBody AnalysisRequest request) {
        return prReviewService.review(request);
    }
}