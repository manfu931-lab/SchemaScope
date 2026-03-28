package com.schemascope.service;

import com.schemascope.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrReviewReportBuilder {

    public PrReviewReport build(AnalysisRequest request,
                                List<ImpactResult> results,
                                GroupedImpactResults groupedResults,
                                ImpactSurfaceSummary surfaceSummary,
                                TestExecutionPlan testExecutionPlan,
                                EvidenceGraphExport evidenceGraph) {
        return build(request, results, groupedResults, surfaceSummary, testExecutionPlan, evidenceGraph, null);
    }

    public PrReviewReport build(AnalysisRequest request,
                                List<ImpactResult> results,
                                GroupedImpactResults groupedResults,
                                ImpactSurfaceSummary surfaceSummary,
                                TestExecutionPlan testExecutionPlan,
                                EvidenceGraphExport evidenceGraph,
                                AiReviewResult aiReview) {
        List<ImpactResult> safeResults = results == null ? new ArrayList<>() : results;
        GroupedImpactResults safeGroupedResults = groupedResults == null
                ? new GroupedImpactResults(new ArrayList<>(), new ArrayList<>())
                : groupedResults;
        ImpactSurfaceSummary safeSurfaceSummary = surfaceSummary == null
                ? new ImpactSurfaceSummary(new ArrayList<>(), new ArrayList<>())
                : surfaceSummary;
        TestExecutionPlan safePlan = testExecutionPlan == null ? new TestExecutionPlan() : testExecutionPlan;

        String changeSummary = buildChangeSummary(request);
        RiskLevel overallRiskLevel = resolveOverallRiskLevel(safeResults);
        ReviewVerdict verdict = resolveVerdict(request, safeResults, safeGroupedResults);
        List<ReviewActionItem> actionItems = buildActionItems(
                request,
                safeResults,
                safeGroupedResults,
                safeSurfaceSummary,
                safePlan
        );
        List<String> checklist = buildChecklist(safeResults, safeGroupedResults, safeSurfaceSummary, safePlan);
        String markdownComment = buildMarkdownComment(
                request,
                changeSummary,
                verdict,
                overallRiskLevel,
                safeResults,
                safeGroupedResults,
                safeSurfaceSummary,
                safePlan,
                evidenceGraph,
                aiReview,
                actionItems
        );

        return new PrReviewReport(
                request == null ? null : request.getProjectName(),
                "SchemaScope PR Review - " + verdict,
                changeSummary,
                verdict,
                overallRiskLevel,
                safeResults.size(),
                safeGroupedResults.getDirectResults().size(),
                safeGroupedResults.getIndirectResults().size(),
                new ArrayList<>(safeResults),
                safeGroupedResults,
                actionItems,
                checklist,
                safeSurfaceSummary.getImpactedEndpoints(),
                safeSurfaceSummary.getSuggestedTests(),
                safePlan,
                evidenceGraph,
                aiReview,
                markdownComment
        );
    }

    private String buildChangeSummary(AnalysisRequest request) {
        if (request == null) {
            return "Schema change review";
        }

        if (request.getChangeType() != null && !request.getChangeType().isBlank()) {
            String tableName = valueOrFallback(request.getTableName(), "unknown_table");
            if (request.getColumnName() != null && !request.getColumnName().isBlank()) {
                return request.getChangeType() + " " + tableName + "." + request.getColumnName();
            }
            return request.getChangeType() + " " + tableName;
        }

        if (request.getOldSchemaPath() != null && request.getNewSchemaPath() != null) {
            return "Schema diff review: " + request.getOldSchemaPath() + " -> " + request.getNewSchemaPath();
        }

        return "Schema change review";
    }

    private RiskLevel resolveOverallRiskLevel(List<ImpactResult> results) {
        if (results.stream().anyMatch(result -> result.getRiskLevel() == RiskLevel.HIGH)) {
            return RiskLevel.HIGH;
        }
        if (results.stream().anyMatch(result -> result.getRiskLevel() == RiskLevel.MEDIUM)) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private ReviewVerdict resolveVerdict(AnalysisRequest request,
                                         List<ImpactResult> results,
                                         GroupedImpactResults groupedResults) {
        if (results.isEmpty()) {
            return ReviewVerdict.APPROVE;
        }

        boolean hasHighDirect = groupedResults.getDirectResults().stream()
                .anyMatch(result -> result.getRiskLevel() == RiskLevel.HIGH);

        boolean isBreakingStyleChange = request != null
                && ("DROP_COLUMN".equalsIgnoreCase(request.getChangeType())
                || "DROP_TABLE".equalsIgnoreCase(request.getChangeType()));

        if (hasHighDirect && isBreakingStyleChange) {
            return ReviewVerdict.BLOCK;
        }

        if (hasHighDirect || groupedResults.getDirectResults().size() >= 2) {
            return ReviewVerdict.REVIEW_REQUIRED;
        }

        if (results.stream().anyMatch(result -> result.getRiskLevel() == RiskLevel.HIGH)) {
            return ReviewVerdict.REVIEW_REQUIRED;
        }

        return ReviewVerdict.APPROVE;
    }

    private List<ReviewActionItem> buildActionItems(AnalysisRequest request,
                                                    List<ImpactResult> results,
                                                    GroupedImpactResults groupedResults,
                                                    ImpactSurfaceSummary surfaceSummary,
                                                    TestExecutionPlan testExecutionPlan) {
        List<ReviewActionItem> items = new ArrayList<>();

        if (!groupedResults.getDirectResults().isEmpty()) {
            String directObjects = groupedResults.getDirectResults().stream()
                    .map(ImpactResult::getAffectedObject)
                    .limit(3)
                    .collect(Collectors.joining(", "));

            items.add(new ReviewActionItem(
                    "核对直接受影响对象",
                    "优先检查直接影响链上的对象： " + directObjects,
                    RiskLevel.HIGH
            ));
        }

        if (!surfaceSummary.getImpactedEndpoints().isEmpty()) {
            String endpointSummary = surfaceSummary.getImpactedEndpoints().stream()
                    .limit(3)
                    .map(endpoint -> endpoint.getHttpMethod() + " " + endpoint.getPath())
                    .collect(Collectors.joining(", "));

            items.add(new ReviewActionItem(
                    "补充接口回归验证",
                    "本次变更已经关联到接口面，优先验证： " + endpointSummary,
                    RiskLevel.HIGH
            ));
        }

        if (!testExecutionPlan.getPrioritizedExistingTests().isEmpty()) {
            String tests = testExecutionPlan.getPrioritizedExistingTests().stream()
                    .limit(3)
                    .map(SelectedTestCase::getTestClassName)
                    .collect(Collectors.joining(", "));

            items.add(new ReviewActionItem(
                    "优先执行高相关测试",
                    "建议优先执行这些已有测试： " + tests,
                    RiskLevel.MEDIUM
            ));
        }

        if (!testExecutionPlan.getMissingRecommendedTests().isEmpty()) {
            String missing = testExecutionPlan.getMissingRecommendedTests().stream()
                    .limit(2)
                    .map(SelectedTestCase::getTestClassName)
                    .collect(Collectors.joining(", "));

            items.add(new ReviewActionItem(
                    "补齐缺失的回归测试",
                    "以下高相关测试当前不存在，建议补充： " + missing,
                    RiskLevel.MEDIUM
            ));
        }

        if (request != null && ("DROP_COLUMN".equalsIgnoreCase(request.getChangeType())
                || "DROP_TABLE".equalsIgnoreCase(request.getChangeType()))) {
            items.add(new ReviewActionItem(
                    "确认兼容与回滚方案",
                    "当前变更属于破坏性数据库变更，建议在合并前确认回滚脚本、灰度策略和兼容窗口。",
                    RiskLevel.HIGH
            ));
        }

        if (items.isEmpty()) {
            items.add(new ReviewActionItem(
                    "保留基础 Review",
                    "当前未发现明显高风险传播链，但仍建议完成基础 SQL / 接口核对。",
                    RiskLevel.LOW
            ));
        }

        return items;
    }

    private List<String> buildChecklist(List<ImpactResult> results,
                                        GroupedImpactResults groupedResults,
                                        ImpactSurfaceSummary surfaceSummary,
                                        TestExecutionPlan testExecutionPlan) {
        List<String> checklist = new ArrayList<>();

        long evidenceCovered = results.stream()
                .filter(result -> result.getEvidencePath() != null && !result.getEvidencePath().isEmpty())
                .count();

        long highRiskCount = results.stream()
                .filter(result -> result.getRiskLevel() == RiskLevel.HIGH)
                .count();

        checklist.add("Direct impacts: " + groupedResults.getDirectResults().size());
        checklist.add("Indirect impacts: " + groupedResults.getIndirectResults().size());
        checklist.add("High-risk objects: " + highRiskCount);
        checklist.add("Evidence-covered objects: " + evidenceCovered + "/" + results.size());
        checklist.add("Impacted endpoints: " + surfaceSummary.getImpactedEndpoints().size());
        checklist.add("Suggested tests: " + surfaceSummary.getSuggestedTests().size());
        checklist.add("Prioritized existing tests: " + testExecutionPlan.getExistingTestCount());
        checklist.add("Missing recommended tests: " + testExecutionPlan.getMissingTestCount());

        return checklist;
    }

    private String buildMarkdownComment(AnalysisRequest request,
                                        String changeSummary,
                                        ReviewVerdict verdict,
                                        RiskLevel overallRiskLevel,
                                        List<ImpactResult> results,
                                        GroupedImpactResults groupedResults,
                                        ImpactSurfaceSummary surfaceSummary,
                                        TestExecutionPlan testExecutionPlan,
                                        EvidenceGraphExport evidenceGraph,
                                        AiReviewResult aiReview,
                                        List<ReviewActionItem> actionItems) {
        StringBuilder sb = new StringBuilder();

        sb.append("# SchemaScope PR Review — ").append(verdict).append("\n\n");
        sb.append("- Project: ").append(valueOrFallback(request == null ? null : request.getProjectName(), "unknown_project")).append("\n");
        sb.append("- Change: ").append(changeSummary).append("\n");
        sb.append("- Overall risk: ").append(overallRiskLevel).append("\n");
        sb.append("- Direct impacts: ").append(groupedResults.getDirectResults().size()).append("\n");
        sb.append("- Indirect impacts: ").append(groupedResults.getIndirectResults().size()).append("\n");
        sb.append("- Impacted endpoints: ").append(surfaceSummary.getImpactedEndpoints().size()).append("\n");
        sb.append("- Suggested tests: ").append(surfaceSummary.getSuggestedTests().size()).append("\n");
        sb.append("- Prioritized existing tests: ").append(testExecutionPlan.getExistingTestCount()).append("\n");
        sb.append("- Missing recommended tests: ").append(testExecutionPlan.getMissingTestCount()).append("\n");

        if (evidenceGraph != null) {
            sb.append("- Graph nodes: ").append(evidenceGraph.getNodes().size()).append("\n");
            sb.append("- Graph edges: ").append(evidenceGraph.getEdges().size()).append("\n");
        }
        if (aiReview != null) {
            sb.append("- AI review mode: ").append(aiReview.getMode()).append("\n");
            sb.append("- AI provider: ").append(aiReview.getProvider()).append("\n");
        }
        sb.append("\n");

        sb.append("## Top affected components\n");
        if (results.isEmpty()) {
            sb.append("- No affected components identified.\n\n");
        } else {
            int index = 1;
            for (ImpactResult result : results) {
                sb.append(index++)
                        .append(". ")
                        .append(result.getAffectedObject())
                        .append(" [")
                        .append(result.getAffectedType())
                        .append("] - ")
                        .append(result.getRiskLevel())
                        .append(" / ")
                        .append(result.getRelationLevel())
                        .append(" / score=")
                        .append(String.format("%.1f", result.getRiskScore()))
                        .append("\n");
            }
            sb.append("\n");
        }

        sb.append("## AI review augmentation\n");
        if (aiReview == null) {
            sb.append("- No AI review result generated.\n\n");
        } else {
            sb.append("- Summary: ").append(aiReview.getExecutiveSummary()).append("\n");
            for (AiReviewFinding finding : aiReview.getFindings()) {
                sb.append("- ")
                        .append(finding.getTitle())
                        .append(" [")
                        .append(finding.getRiskLevel())
                        .append(", confidence=")
                        .append(String.format("%.2f", finding.getConfidence()))
                        .append("]: ")
                        .append(finding.getDetail())
                        .append("\n");
            }
            if (!aiReview.getRecommendedChecks().isEmpty()) {
                sb.append("- Recommended checks:\n");
                for (String check : aiReview.getRecommendedChecks()) {
                    sb.append("  - ").append(check).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("## Impacted endpoints\n");
        if (surfaceSummary.getImpactedEndpoints().isEmpty()) {
            sb.append("- No controller endpoint surfaced.\n\n");
        } else {
            for (ApiEndpointImpact endpoint : surfaceSummary.getImpactedEndpoints()) {
                sb.append("- ")
                        .append(endpoint.getHttpMethod())
                        .append(" ")
                        .append(endpoint.getPath())
                        .append(" (")
                        .append(endpoint.getOwnerController())
                        .append(", ")
                        .append(endpoint.getRiskLevel())
                        .append(", ")
                        .append(endpoint.getRelationLevel())
                        .append(")\n");
            }
            sb.append("\n");
        }

        sb.append("## Test execution plan\n");
        if (testExecutionPlan.getPrioritizedExistingTests().isEmpty()
                && testExecutionPlan.getMissingRecommendedTests().isEmpty()) {
            sb.append("- No targeted test plan generated.\n\n");
        } else {
            for (SelectedTestCase testCase : testExecutionPlan.getPrioritizedExistingTests()) {
                sb.append("- RUN ")
                        .append(testCase.getTestClassName())
                        .append(" (")
                        .append(testCase.getPriority())
                        .append(", score=")
                        .append(String.format("%.2f", testCase.getScore()))
                        .append("): ")
                        .append(testCase.getReason())
                        .append("\n");
            }
            for (SelectedTestCase testCase : testExecutionPlan.getMissingRecommendedTests()) {
                sb.append("- ADD ")
                        .append(testCase.getTestClassName())
                        .append(" (")
                        .append(testCase.getPriority())
                        .append(", score=")
                        .append(String.format("%.2f", testCase.getScore()))
                        .append("): ")
                        .append(testCase.getReason())
                        .append("\n");
            }
            sb.append("\n");
        }

        sb.append("## Evidence graph\n");
        if (evidenceGraph == null || evidenceGraph.getMermaid() == null || evidenceGraph.getMermaid().isBlank()) {
            sb.append("- No graph export generated.\n\n");
        } else {
            sb.append("```mermaid\n");
            sb.append(evidenceGraph.getMermaid()).append("\n");
            sb.append("```\n\n");
        }

        sb.append("## Required actions\n");
        for (ReviewActionItem item : actionItems) {
            sb.append("- [ ] ")
                    .append(item.getTitle())
                    .append(" (")
                    .append(item.getPriority())
                    .append("): ")
                    .append(item.getDetail())
                    .append("\n");
        }
        sb.append("\n");

        sb.append("## Evidence sample\n");
        if (!results.isEmpty() && results.get(0).getEvidencePath() != null) {
            results.get(0).getEvidencePath().stream()
                    .limit(5)
                    .forEach(step -> sb.append("- ").append(step).append("\n"));
        } else {
            sb.append("- No evidence path available.\n");
        }

        return sb.toString();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}