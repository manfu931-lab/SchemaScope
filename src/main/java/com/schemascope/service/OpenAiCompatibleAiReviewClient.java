package com.schemascope.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemascope.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleAiReviewClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String provider;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public OpenAiCompatibleAiReviewClient(ObjectMapper objectMapper,
                                          @Value("${schemascope.ai.provider:openai-compatible}") String provider,
                                          @Value("${schemascope.ai.base-url:https://api.deepseek.com/v1}") String baseUrl,
                                          @Value("${schemascope.ai.api-key:sk-b23650f26e744c6f823dbaa9d1c806b4}") String apiKey,
                                          @Value("${schemascope.ai.model:deepseek-chat}") String model,
                                          @Value("${schemascope.ai.timeout-seconds:30}") long timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.provider = provider;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(timeoutSeconds, 5)))
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank()
                && baseUrl != null && !baseUrl.isBlank()
                && model != null && !model.isBlank();
    }

    public AiReviewResult review(AnalysisRequest request, PrReviewReport report) {
        if (!isConfigured()) {
            throw new IllegalStateException("AI client is not configured. Please set schemascope.ai.api-key and related properties.");
        }

        try {
            String payloadJson = objectMapper.writeValueAsString(buildEvidencePayload(request, report));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("temperature", 0.2);
            body.put("response_format", Map.of("type", "json_object"));
            body.put("messages", List.of(
                    Map.of("role", "system", "content", buildSystemPrompt()),
                    Map.of("role", "user", "content", buildUserPrompt(payloadJson))
            ));

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(baseUrl) + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Remote AI returned HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                throw new IllegalStateException("Remote AI response does not contain choices.");
            }

            String content = choices.get(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("Remote AI response content is empty.");
            }

            return parseAssistantContent(content);

        } catch (Exception e) {
            throw new RuntimeException("Remote AI review failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildEvidencePayload(AnalysisRequest request, PrReviewReport report) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("projectName", request == null ? null : request.getProjectName());
        payload.put("changeSummary", report.getChangeSummary());
        payload.put("verdict", report.getVerdict() == null ? null : report.getVerdict().name());
        payload.put("overallRiskLevel", report.getOverallRiskLevel() == null ? null : report.getOverallRiskLevel().name());
        payload.put("directImpactCount", report.getDirectImpactCount());
        payload.put("indirectImpactCount", report.getIndirectImpactCount());

        List<Map<String, Object>> impactedObjects = new ArrayList<>();
        if (report.getTopRiskResults() != null) {
            for (ImpactResult result : report.getTopRiskResults()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("affectedObject", result.getAffectedObject());
                item.put("affectedType", result.getAffectedType());
                item.put("riskLevel", result.getRiskLevel() == null ? null : result.getRiskLevel().name());
                item.put("relationLevel", result.getRelationLevel() == null ? null : result.getRelationLevel().name());
                item.put("riskScore", result.getRiskScore());
                item.put("evidencePath", limitList(result.getEvidencePath(), 6));
                impactedObjects.add(item);
            }
        }
        payload.put("topRiskResults", impactedObjects);

        List<Map<String, Object>> endpoints = new ArrayList<>();
        if (report.getImpactedEndpoints() != null) {
            for (ApiEndpointImpact endpoint : report.getImpactedEndpoints()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("ownerController", endpoint.getOwnerController());
                item.put("httpMethod", endpoint.getHttpMethod());
                item.put("path", endpoint.getPath());
                item.put("riskLevel", endpoint.getRiskLevel() == null ? null : endpoint.getRiskLevel().name());
                endpoints.add(item);
            }
        }
        payload.put("impactedEndpoints", endpoints);

        Map<String, Object> testPlan = new LinkedHashMap<>();
        if (report.getTestExecutionPlan() != null) {
            testPlan.put("existingTestCount", report.getTestExecutionPlan().getExistingTestCount());
            testPlan.put("missingTestCount", report.getTestExecutionPlan().getMissingTestCount());
            testPlan.put("summary", report.getTestExecutionPlan().getSummary());
            testPlan.put("prioritizedExistingTests", toSelectedTests(report.getTestExecutionPlan().getPrioritizedExistingTests(), 5));
            testPlan.put("missingRecommendedTests", toSelectedTests(report.getTestExecutionPlan().getMissingRecommendedTests(), 5));
        }
        payload.put("testExecutionPlan", testPlan);

        Map<String, Object> graph = new LinkedHashMap<>();
        if (report.getEvidenceGraph() != null) {
            graph.put("nodeCount", report.getEvidenceGraph().getNodes() == null ? 0 : report.getEvidenceGraph().getNodes().size());
            graph.put("edgeCount", report.getEvidenceGraph().getEdges() == null ? 0 : report.getEvidenceGraph().getEdges().size());
            graph.put("summary", report.getEvidenceGraph().getSummary());
        }
        payload.put("evidenceGraph", graph);

        return payload;
    }

    private List<Map<String, Object>> toSelectedTests(List<SelectedTestCase> tests, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (tests == null) {
            return result;
        }

        int count = 0;
        for (SelectedTestCase test : tests) {
            if (count++ >= limit) {
                break;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("testClassName", test.getTestClassName());
            item.put("score", test.getScore());
            item.put("priority", test.getPriority() == null ? null : test.getPriority().name());
            item.put("existingTest", test.isExistingTest());
            item.put("reason", test.getReason());
            result.add(item);
        }
        return result;
    }

    private List<String> limitList(List<String> input, int limit) {
        List<String> output = new ArrayList<>();
        if (input == null) {
            return output;
        }

        for (int i = 0; i < input.size() && i < limit; i++) {
            output.add(input.get(i));
        }
        return output;
    }

    private String buildSystemPrompt() {
        return """
                You are a senior backend architecture and release reviewer.
                You are given deterministic evidence extracted from a schema impact analysis pipeline.
                Your job is to produce a concise, structured review augmentation.

                Return ONLY JSON with this schema:
                {
                  "executiveSummary": "string",
                  "findings": [
                    {
                      "category": "string",
                      "title": "string",
                      "detail": "string",
                      "riskLevel": "HIGH|MEDIUM|LOW",
                      "confidence": 0.0
                    }
                  ],
                  "recommendedChecks": ["string"],
                  "releaseNotes": ["string"]
                }

                Rules:
                - Do not invent components or endpoints that are not present in the evidence.
                - Prefer concrete operational guidance over generic statements.
                - Keep findings grounded in the provided evidence.
                - Do not wrap the JSON in markdown fences.
                """;
    }

    private String buildUserPrompt(String payloadJson) {
        return "Evidence payload:\n" + payloadJson;
    }

    private AiReviewResult parseAssistantContent(String content) throws IOException {
        String json = extractJsonObject(content);
        JsonNode root = objectMapper.readTree(json);

        String executiveSummary = root.path("executiveSummary").asText("");

        List<AiReviewFinding> findings = new ArrayList<>();
        JsonNode findingNodes = root.path("findings");
        if (findingNodes.isArray()) {
            for (JsonNode findingNode : findingNodes) {
                findings.add(new AiReviewFinding(
                        findingNode.path("category").asText("AI_FINDING"),
                        findingNode.path("title").asText("AI finding"),
                        findingNode.path("detail").asText(""),
                        parseRiskLevel(findingNode.path("riskLevel").asText("MEDIUM")),
                        findingNode.path("confidence").asDouble(0.75)
                ));
            }
        }

        List<String> recommendedChecks = new ArrayList<>();
        JsonNode checkNodes = root.path("recommendedChecks");
        if (checkNodes.isArray()) {
            for (JsonNode item : checkNodes) {
                recommendedChecks.add(item.asText());
            }
        }

        List<String> releaseNotes = new ArrayList<>();
        JsonNode noteNodes = root.path("releaseNotes");
        if (noteNodes.isArray()) {
            for (JsonNode item : noteNodes) {
                releaseNotes.add(item.asText());
            }
        }

        return new AiReviewResult(
                provider,
                "REMOTE_LLM",
                executiveSummary,
                findings,
                recommendedChecks,
                releaseNotes
        );
    }

    private RiskLevel parseRiskLevel(String value) {
        try {
            return RiskLevel.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return RiskLevel.MEDIUM;
        }
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalStateException("Could not find JSON object in AI response.");
        }
        return text.substring(start, end + 1);
    }

    private String normalizeBaseUrl(String rawBaseUrl) {
        if (rawBaseUrl.endsWith("/")) {
            return rawBaseUrl.substring(0, rawBaseUrl.length() - 1);
        }
        return rawBaseUrl;
    }
}