package com.schemascope.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnMockAnalysisResults() throws Exception {
        String requestBody = """
                {
                  "projectName": "petclinic",
                  "projectPath": "/benchmark/petclinic",
                  "oldSchemaPath": "schema_v1.sql",
                  "newSchemaPath": "schema_v2.sql"
                }
                """;

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // 修改了这里
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeId").value("chg-001"))
                .andExpect(jsonPath("$[0].affectedObject").value("/api/orders/list"))
                .andExpect(jsonPath("$[0].riskLevel").value("HIGH"))
                .andExpect(jsonPath("$[1].affectedType").value("METHOD"));
    }
}