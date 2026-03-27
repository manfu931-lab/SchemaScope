package com.schemascope.api;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.service.DefenseShowcaseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DefenseShowcaseService defenseShowcaseService;

    public DemoController(DefenseShowcaseService defenseShowcaseService) {
        this.defenseShowcaseService = defenseShowcaseService;
    }

    @PostMapping("/showcase")
    public DefenseShowcasePack buildShowcase(@RequestBody AnalysisRequest request) {
        return defenseShowcaseService.buildShowcase(request);
    }
}