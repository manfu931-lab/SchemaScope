package com.schemascope.api;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.EvidenceGraphExport;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.service.PrReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final PrReviewService prReviewService;

    public GraphController(PrReviewService prReviewService) {
        this.prReviewService = prReviewService;
    }

    @PostMapping("/evidence")
    public EvidenceGraphExport exportEvidenceGraph(@RequestBody AnalysisRequest request) {
        PrReviewReport report = prReviewService.review(request);
        return report.getEvidenceGraph();
    }
}