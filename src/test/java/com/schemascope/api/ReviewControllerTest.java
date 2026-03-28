package com.schemascope.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnPullRequestReviewReportWithAiReview() throws Exception {
        String requestBody = """
                {
                  "projectName": "sql-demo-project",
                  "projectPath": "src/test/resources/fixture/sql-demo-project",
                  "changeType": "DROP_COLUMN",
                  "tableName": "owners",
                  "columnName": "last_name",
                  "oldType": "VARCHAR(80)",
                  "newType": null,
                  "sourceFile": "manual-review"
                }
                """;

        mockMvc.perform(post("/api/review/pr")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("sql-demo-project"))
                .andExpect(jsonPath("$.verdict").value("BLOCK"))
                .andExpect(jsonPath("$.aiReview").exists())
                .andExpect(jsonPath("$.aiReview.mode").exists())
                .andExpect(jsonPath("$.aiReview.provider").exists())
                .andExpect(jsonPath("$.aiReview.executiveSummary").exists())
                .andExpect(jsonPath("$.aiReview.findings").isArray())
                .andExpect(jsonPath("$.aiReview.recommendedChecks").isArray())
                .andExpect(content().string(containsString("AI review augmentation")))
                .andExpect(content().string(containsString("OwnerController")))
                .andExpect(content().string(containsString("graph TD")));
    }
}