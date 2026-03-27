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
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnEvidenceGraphExport() throws Exception {
        String requestBody = """
                {
                  "projectName": "sql-demo-project",
                  "projectPath": "src/test/resources/fixture/sql-demo-project",
                  "changeType": "DROP_COLUMN",
                  "tableName": "owners",
                  "columnName": "last_name",
                  "oldType": "VARCHAR(80)",
                  "newType": null,
                  "sourceFile": "manual-graph"
                }
                """;

        mockMvc.perform(post("/api/graph/evidence")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.mermaid").exists())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(content().string(containsString("graph TD")))
                .andExpect(content().string(containsString("OwnerController")))
                .andExpect(content().string(containsString("/owners")));
    }
}