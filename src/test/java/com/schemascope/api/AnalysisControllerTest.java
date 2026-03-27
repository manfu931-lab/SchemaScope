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
                .andExpect(jsonPath("$[1].affectedType").value("METHOD"))
                .andExpect(jsonPath("$[0].relationLevel").exists());
    }

        @Test
        void shouldReturnAnalysisResultsForAlterColumnType() throws Exception {
                String requestBody = """
                        {
                          "projectName": "petclinic",
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
                          "projectName": "schemascope-self",
                          "projectPath": ".",
                          "oldSchemaPath": "src/test/resources/schema/schema_v1.sql",
                          "newSchemaPath": "src/test/resources/schema/schema_v2.sql"
                        }
                        """;

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].changeId").exists())
                .andExpect(jsonPath("$[0].relationLevel").exists());
        }
        
        @Test
        void shouldReturnMappedResultsForExternalProject() throws Exception {
        String requestBody = """
                {
                "projectName": "spring-petclinic",
                "projectPath": "D:/download/SchemaScope/benchmark/spring-petclinic",
                "changeType": "DROP_COLUMN",
                "tableName": "owners",
                "columnName": "last_name",
                "oldType": "VARCHAR(80)",
                "newType": null,
                "sourceFile": "manual-test"
                }
                """;

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].changeId").value("chg-drop-column-owners-last-name"))
                .andExpect(jsonPath("$[0].affectedObject").value("Owner"))
                .andExpect(jsonPath("$[0].affectedType").value("ENTITY"))
                .andExpect(jsonPath("$[0].riskLevel").value("HIGH"))
                .andExpect(jsonPath("$[1].affectedObject").value("OwnerController"))
                .andExpect(jsonPath("$[0].relationLevel").value("DIRECT"))
                .andExpect(jsonPath("$[1].relationLevel").value("INDIRECT"));
        
}

}