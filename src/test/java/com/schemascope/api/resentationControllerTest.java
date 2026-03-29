package com.schemascope.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.ReviewVerdict;
import com.schemascope.domain.RiskLevel;
import com.schemascope.service.DefenseShowcaseService;
import com.schemascope.service.PresentationViewAssembler;
import com.schemascope.service.PrReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PresentationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PresentationViewAssembler.class)
class PresentationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrReviewService prReviewService;

    @MockBean
    private DefenseShowcaseService defenseShowcaseService;

    @Test
    void shouldReturnReviewPageView() throws Exception {
        PrReviewReport report = new PrReviewReport();
        report.setProjectName("sql-demo-project");
        report.setChangeSummary("DROP_COLUMN owners.last_name");
        report.setVerdict(ReviewVerdict.REVIEW_REQUIRED);
        report.setOverallRiskLevel(RiskLevel.HIGH);
        report.setMarkdownComment("# SchemaScope PR Review");

        given(prReviewService.review(any(AnalysisRequest.class))).willReturn(report);

        String requestBody = objectMapper.writeValueAsString(new AnalysisRequest(
                "sql-demo-project",
                "src/test/resources/fixture/sql-demo-project",
                null,
                null,
                "DROP_COLUMN",
                "owners",
                "last_name",
                "VARCHAR(80)",
                null,
                "manual-review"
        ));

        mockMvc.perform(post("/api/view/review-page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(containsString("SchemaScope PR Review")))
                .andExpect(jsonPath("$.verdict").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.metricCards").isArray())
                .andExpect(jsonPath("$.markdownComment").exists());
    }

    @Test
    void shouldReturnShowcaseDashboardView() throws Exception {
        DefenseShowcasePack pack = new DefenseShowcasePack();
        pack.setProjectName("sql-demo-project");
        pack.setDemoTitle("SchemaScope Defense Showcase");
        pack.setExecutiveSummary("The change has strong evidence-backed impact.");
        pack.setVerdict(ReviewVerdict.REVIEW_REQUIRED);
        pack.setOverallRiskLevel(RiskLevel.HIGH);
        pack.setMarkdownBrief("# SchemaScope Showcase");

        PrReviewReport report = new PrReviewReport();
        report.setProjectName("sql-demo-project");
        report.setChangeSummary("DROP_COLUMN owners.last_name");
        report.setVerdict(ReviewVerdict.REVIEW_REQUIRED);
        report.setOverallRiskLevel(RiskLevel.HIGH);
        pack.setReviewReport(report);

        given(defenseShowcaseService.buildShowcase(any(AnalysisRequest.class))).willReturn(pack);

        String requestBody = objectMapper.writeValueAsString(new AnalysisRequest(
                "sql-demo-project",
                "src/test/resources/fixture/sql-demo-project",
                null,
                null,
                "DROP_COLUMN",
                "owners",
                "last_name",
                "VARCHAR(80)",
                null,
                "manual-showcase"
        ));

        mockMvc.perform(post("/api/view/showcase-dashboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("SchemaScope Defense Showcase"))
                .andExpect(jsonPath("$.executiveSummary").value(containsString("evidence-backed")))
                .andExpect(jsonPath("$.reviewPage").exists())
                .andExpect(jsonPath("$.markdownBrief").exists());
    }
}