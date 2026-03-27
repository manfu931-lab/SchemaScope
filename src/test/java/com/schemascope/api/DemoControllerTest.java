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
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDefenseShowcasePack() throws Exception {
        String requestBody = """
                {
                  "projectName": "sql-demo-project",
                  "projectPath": "src/test/resources/fixture/sql-demo-project",
                  "changeType": "DROP_COLUMN",
                  "tableName": "owners",
                  "columnName": "last_name",
                  "oldType": "VARCHAR(80)",
                  "newType": null,
                  "sourceFile": "manual-showcase"
                }
                """;

        mockMvc.perform(post("/api/demo/showcase")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demoTitle").value("SchemaScope Defense Showcase"))
                .andExpect(jsonPath("$.executiveSummary").exists())
                .andExpect(jsonPath("$.metricCards").isArray())
                .andExpect(jsonPath("$.coreHighlights").isArray())
                .andExpect(jsonPath("$.demoSteps").isArray())
                .andExpect(jsonPath("$.defenseTalkingPoints").isArray())
                .andExpect(jsonPath("$.reviewReport").exists())
                .andExpect(jsonPath("$.evidenceGraph").exists())
                .andExpect(jsonPath("$.markdownBrief").exists())
                .andExpect(content().string(containsString("SchemaScope Defense Showcase")))
                .andExpect(content().string(containsString("OwnerController")))
                .andExpect(content().string(containsString("graph TD")));
    }
}