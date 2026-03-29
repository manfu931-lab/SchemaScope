package com.schemascope.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemascope.domain.AiReviewResult;
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.EvidenceGraphExport;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.service.PrReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrReviewService prReviewService;

    @Test
    void shouldReturnPrReviewReportWithAiAndGraph() throws Exception {
        PrReviewReport report = new PrReviewReport();
        report.setProjectName("sql-demo-project");
        report.setChangeSummary("DROP_COLUMN owners.last_name");
        report.setMarkdownComment("# SchemaScope PR Review\n\nOwnerRepository impacted.");

        AiReviewResult aiReview = new AiReviewResult();
        aiReview.setExecutiveSummary("This schema change should be reviewed before release.");
        report.setAiReview(aiReview);

        EvidenceGraphExport evidenceGraph = new EvidenceGraphExport();
        report.setEvidenceGraph(evidenceGraph);

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

        mockMvc.perform(post("/api/review/pr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("sql-demo-project"))
                .andExpect(jsonPath("$.changeSummary").value(containsString("owners.last_name")))
                .andExpect(jsonPath("$.aiReview").exists())
                .andExpect(jsonPath("$.evidenceGraph").exists())
                .andExpect(jsonPath("$.markdownComment").exists())
                .andExpect(content().string(containsString("OwnerRepository")));
    }
}