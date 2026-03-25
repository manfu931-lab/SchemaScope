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
                  "changeType": "DROP_COLUMN",
                  "tableName": "orders",
                  "columnName": "status",
                  "oldType": "varchar(16)",
                  "newType": null,
                  "sourceFile": "V12__drop_orders_status.sql"
                }
                """;
    
        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeId").value("chg-drop-column-orders-status"))
                .andExpect(jsonPath("$[0].affectedObject").value("/api/orders/list"))
                .andExpect(jsonPath("$[0].riskLevel").value("HIGH"))
                .andExpect(jsonPath("$[1].affectedType").value("METHOD"));
    }

        @Test
        void shouldReturnAnalysisResultsForAlterColumnType() throws Exception {
                String requestBody = """
                        {
                          "projectName": "petclinic",
                          "projectPath": "/benchmark/petclinic",
                          "changeType": "ALTER_COLUMN_TYPE",
                          "tableName": "users",
                          "columnName": "email",
                          "oldType": "varchar(64)",
                          "newType": "varchar(128)",
                          "sourceFile": "V13__alter_users_email_type.sql"
                        }
                        """;

        mockMvc.perform(post("/api/analysis")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeId").value("chg-alter-column-type-users-email"))
                .andExpect(jsonPath("$[0].affectedObject").value("UserRepository.updateEmail"))
                .andExpect(jsonPath("$[0].affectedType").value("METHOD"))
                .andExpect(jsonPath("$[1].affectedObject").value("/api/users/profile/update"));
        }

        @Test
        void shouldReturnAnalysisResultsFromRealSchemaFiles() throws Exception {
        String requestBody = """
                {
                "projectName": "demo-project",
                "projectPath": "/benchmark/demo-project",
                "oldSchemaPath": "src/test/resources/schema/schema_v1.sql",
                "newSchemaPath": "src/test/resources/schema/schema_v2.sql"
                }
                """;

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].changeId").exists());
        }       

}