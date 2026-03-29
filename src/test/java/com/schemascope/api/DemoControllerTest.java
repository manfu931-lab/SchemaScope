package com.schemascope.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.domain.EvidenceGraphExport;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.service.DefenseShowcaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoController.class)
@AutoConfigureMockMvc(addFilters = false)
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DefenseShowcaseService defenseShowcaseService;

    @Test
    void shouldReturnDefenseShowcasePack() throws Exception {
        DefenseShowcasePack pack = new DefenseShowcasePack();
        pack.setProjectName("sql-demo-project");
        pack.setMarkdownBrief("# SchemaScope Showcase\n\nOwnerRepository impacted.");
        pack.setReviewReport(new PrReviewReport());
        pack.setEvidenceGraph(new EvidenceGraphExport());

        // 这些字段按你当前项目通常是列表，先给空列表，保证 JSON 结构稳定
        pack.setCoreHighlights(new ArrayList<>());
        pack.setDemoSteps(new ArrayList<>());
        pack.setDefenseTalkingPoints(new ArrayList<>());
        pack.setMetricCards(new ArrayList<>());

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

        mockMvc.perform(post("/api/demo/showcase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("sql-demo-project"))
                .andExpect(jsonPath("$.reviewReport").exists())
                .andExpect(jsonPath("$.evidenceGraph").exists())
                .andExpect(jsonPath("$.markdownBrief").exists())
                .andExpect(content().string(containsString("SchemaScope")));
    }
}