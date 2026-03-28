package com.schemascope.service.impl;

import com.schemascope.domain.*;
import com.schemascope.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockPrReviewService implements PrReviewService {

    private final AnalysisService analysisService;
    private final ImpactResultGrouper impactResultGrouper;
    private final PrReviewReportBuilder prReviewReportBuilder;
    private final ImpactSurfaceBuilder impactSurfaceBuilder;
    private final TestImpactPlanner testImpactPlanner;
    private final EvidenceGraphExporter evidenceGraphExporter;
    private final AiReviewService aiReviewService;

    public MockPrReviewService(AnalysisService analysisService,
                               ImpactResultGrouper impactResultGrouper,
                               PrReviewReportBuilder prReviewReportBuilder,
                               ImpactSurfaceBuilder impactSurfaceBuilder,
                               TestImpactPlanner testImpactPlanner,
                               EvidenceGraphExporter evidenceGraphExporter) {
        this(analysisService,
                impactResultGrouper,
                prReviewReportBuilder,
                impactSurfaceBuilder,
                testImpactPlanner,
                evidenceGraphExporter,
                (request, report) -> new RuleBasedAiReviewService().review(request, report));
    }

    @Autowired
    public MockPrReviewService(AnalysisService analysisService,
                               ImpactResultGrouper impactResultGrouper,
                               PrReviewReportBuilder prReviewReportBuilder,
                               ImpactSurfaceBuilder impactSurfaceBuilder,
                               TestImpactPlanner testImpactPlanner,
                               EvidenceGraphExporter evidenceGraphExporter,
                               AiReviewService aiReviewService) {
        this.analysisService = analysisService;
        this.impactResultGrouper = impactResultGrouper;
        this.prReviewReportBuilder = prReviewReportBuilder;
        this.impactSurfaceBuilder = impactSurfaceBuilder;
        this.testImpactPlanner = testImpactPlanner;
        this.evidenceGraphExporter = evidenceGraphExporter;
        this.aiReviewService = aiReviewService;
    }

    @Override
    public PrReviewReport review(AnalysisRequest request) {
        List<ImpactResult> results = analysisService.analyze(request);
        GroupedImpactResults groupedResults = impactResultGrouper.group(results);
        ImpactSurfaceSummary surfaceSummary = buildSurfaceSummarySafely(request, results);
        TestExecutionPlan testExecutionPlan = testImpactPlanner.buildPlan(results, surfaceSummary);

        PrReviewReport baseReport = prReviewReportBuilder.build(
                request,
                results,
                groupedResults,
                surfaceSummary,
                testExecutionPlan,
                null,
                null
        );

        EvidenceGraphExport evidenceGraph = evidenceGraphExporter.export(request, baseReport);

        PrReviewReport graphReadyReport = prReviewReportBuilder.build(
                request,
                results,
                groupedResults,
                surfaceSummary,
                testExecutionPlan,
                evidenceGraph,
                null
        );

        AiReviewResult aiReview = aiReviewService.review(request, graphReadyReport);

        return prReviewReportBuilder.build(
                request,
                results,
                groupedResults,
                surfaceSummary,
                testExecutionPlan,
                evidenceGraph,
                aiReview
        );
    }

    private ImpactSurfaceSummary buildSurfaceSummarySafely(AnalysisRequest request, List<ImpactResult> results) {
        try {
            if (request == null || request.getProjectPath() == null || request.getProjectPath().isBlank()) {
                return new ImpactSurfaceSummary();
            }
            return impactSurfaceBuilder.build(request.getProjectPath(), results);
        } catch (Exception e) {
            return new ImpactSurfaceSummary();
        }
    }
}