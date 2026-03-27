package com.schemascope.service;

import com.schemascope.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefenseShowcaseBuilder {

    public DefenseShowcasePack build(AnalysisRequest request, PrReviewReport report) {
        if (report == null) {
            return new DefenseShowcasePack(
                    request == null ? null : request.getProjectName(),
                    "SchemaScope Defense Showcase",
                    "No review report available.",
                    ReviewVerdict.APPROVE,
                    RiskLevel.LOW,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null,
                    null,
                    "# SchemaScope Showcase\n\nNo report generated."
            );
        }

        EvidenceGraphExport graph = report.getEvidenceGraph();
        TestExecutionPlan testPlan = report.getTestExecutionPlan();

        List<DefenseMetricCard> metricCards = buildMetricCards(report, graph, testPlan);
        List<String> coreHighlights = buildCoreHighlights(report, graph, testPlan);
        List<String> demoSteps = buildDemoSteps(report);
        List<String> defenseTalkingPoints = buildDefenseTalkingPoints(report, graph, testPlan);
        String markdownBrief = buildMarkdownBrief(report, graph, testPlan);

        return new DefenseShowcasePack(
                report.getProjectName(),
                "SchemaScope Defense Showcase",
                buildExecutiveSummary(report, graph, testPlan),
                report.getVerdict(),
                report.getOverallRiskLevel(),
                metricCards,
                coreHighlights,
                demoSteps,
                defenseTalkingPoints,
                report,
                graph,
                markdownBrief
        );
    }

    private List<DefenseMetricCard> buildMetricCards(PrReviewReport report,
                                                     EvidenceGraphExport graph,
                                                     TestExecutionPlan testPlan) {
        List<DefenseMetricCard> cards = new ArrayList<>();

        cards.add(new DefenseMetricCard(
                "受影响对象",
                String.valueOf(report.getTotalImpactedObjects()),
                "Direct " + report.getDirectImpactCount() + " / Indirect " + report.getIndirectImpactCount()
        ));

        cards.add(new DefenseMetricCard(
                "接口影响",
                String.valueOf(report.getImpactedEndpoints() == null ? 0 : report.getImpactedEndpoints().size()),
                "关联受影响 endpoint 数量"
        ));

        cards.add(new DefenseMetricCard(
                "测试计划",
                String.valueOf(testPlan == null ? 0 : testPlan.getExistingTestCount()),
                "现有高相关测试；缺失 " + (testPlan == null ? 0 : testPlan.getMissingTestCount())
        ));

        cards.add(new DefenseMetricCard(
                "证据图",
                String.valueOf(graph == null ? 0 : graph.getNodes().size()),
                "Graph nodes；edges " + (graph == null ? 0 : graph.getEdges().size())
        ));

        return cards;
    }

    private List<String> buildCoreHighlights(PrReviewReport report,
                                             EvidenceGraphExport graph,
                                             TestExecutionPlan testPlan) {
        List<String> highlights = new ArrayList<>();

        highlights.add("证据链从 Schema Change 一直贯通到 SQL、组件、接口和测试计划。");
        highlights.add("PR 审查结果可直接输出 verdict、action items 和 markdown comment。");

        if (graph != null) {
            highlights.add("支持导出跨层 evidence graph，可直接渲染 Mermaid。");
        }

        if (testPlan != null && testPlan.getExistingTestCount() > 0) {
            highlights.add("能够对现有测试进行优先级排序，而不是只给静态提示。");
        }

        if (report.getImpactedEndpoints() != null && !report.getImpactedEndpoints().isEmpty()) {
            highlights.add("能够定位受影响 API endpoint，而不只是受影响 Controller。");
        }

        return highlights;
    }

    private List<String> buildDemoSteps(PrReviewReport report) {
        List<String> steps = new ArrayList<>();

        steps.add("先展示 schema 变更输入，例如 DROP_COLUMN owners.last_name。");
        steps.add("再展示 evidence-driven 的 PR 审查结果，包括 verdict 和高风险对象。");
        steps.add("切到 evidence graph，说明传播路径：Schema -> SQL -> Repository -> Service -> Controller。");
        steps.add("展示 impacted endpoints 和 test execution plan，说明系统如何支持研发验证闭环。");
        steps.add("最后用 markdown comment 展示系统可直接嵌入 PR review 流程。");

        return steps;
    }

    private List<String> buildDefenseTalkingPoints(PrReviewReport report,
                                                   EvidenceGraphExport graph,
                                                   TestExecutionPlan testPlan) {
        List<String> points = new ArrayList<>();

        points.add("这个系统不是只做 schema diff，而是做 evidence-driven impact analysis。");
        points.add("相比只靠类名猜测的方法，这里先命中 SQL 证据，再向上游传播，解释性更强。");
        points.add("系统输出不只给 impacted objects，还给 API、tests、review verdict 和 graph。");
        points.add("这个设计更贴近真实研发流程：发现影响、审查风险、安排验证、输出图示。");

        if (graph != null) {
            points.add("图导出层把原来的文字 evidence path 升级成可视化依赖图，更适合答辩和展示。");
        }

        if (testPlan != null && testPlan.getExistingTestCount() > 0) {
            points.add("测试计划层把测试影响从提示升级成排序后的执行建议，更像工程治理工具。");
        }

        return points;
    }

    private String buildExecutiveSummary(PrReviewReport report,
                                         EvidenceGraphExport graph,
                                         TestExecutionPlan testPlan) {
        return "本次展示包围绕 "
                + report.getChangeSummary()
                + " 构建，系统给出的结论是 "
                + report.getVerdict()
                + " / "
                + report.getOverallRiskLevel()
                + "。同时输出了 "
                + report.getTotalImpactedObjects()
                + " 个受影响对象、"
                + (report.getImpactedEndpoints() == null ? 0 : report.getImpactedEndpoints().size())
                + " 个接口影响、"
                + (testPlan == null ? 0 : testPlan.getExistingTestCount())
                + " 个优先执行测试，以及 "
                + (graph == null ? 0 : graph.getNodes().size())
                + " 个图节点。";
    }

    private String buildMarkdownBrief(PrReviewReport report,
                                      EvidenceGraphExport graph,
                                      TestExecutionPlan testPlan) {
        StringBuilder sb = new StringBuilder();

        sb.append("# SchemaScope Showcase\n\n");
        sb.append("- Project: ").append(report.getProjectName()).append("\n");
        sb.append("- Change: ").append(report.getChangeSummary()).append("\n");
        sb.append("- Verdict: ").append(report.getVerdict()).append("\n");
        sb.append("- Overall risk: ").append(report.getOverallRiskLevel()).append("\n");
        sb.append("- Impacted objects: ").append(report.getTotalImpactedObjects()).append("\n");
        sb.append("- Impacted endpoints: ")
                .append(report.getImpactedEndpoints() == null ? 0 : report.getImpactedEndpoints().size())
                .append("\n");
        sb.append("- Prioritized existing tests: ")
                .append(testPlan == null ? 0 : testPlan.getExistingTestCount())
                .append("\n");
        sb.append("- Graph nodes: ").append(graph == null ? 0 : graph.getNodes().size()).append("\n\n");

        sb.append("## Core highlights\n");
        for (String line : buildCoreHighlights(report, graph, testPlan)) {
            sb.append("- ").append(line).append("\n");
        }

        sb.append("\n## Demo steps\n");
        for (String line : buildDemoSteps(report)) {
            sb.append("- ").append(line).append("\n");
        }

        return sb.toString();
    }
}