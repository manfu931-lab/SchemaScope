package com.schemascope.api;

import com.schemascope.api.dto.AnalysisJobStatusResponse;
import com.schemascope.api.dto.CreateAnalysisJobResponse;
import com.schemascope.service.AnalysisJobService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/jobs")
public class AnalysisJobController {

    private final AnalysisJobService analysisJobService;

    public AnalysisJobController(AnalysisJobService analysisJobService) {
        this.analysisJobService = analysisJobService;
    }

    @PostMapping(value = "/upload-analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateAnalysisJobResponse uploadAnalysisJob(@RequestPart("file") MultipartFile file,
                                                       @RequestParam(value = "projectName", required = false) String projectName,
                                                       @RequestParam(value = "changeType", required = false) String changeType,
                                                       @RequestParam(value = "tableName", required = false) String tableName,
                                                       @RequestParam(value = "columnName", required = false) String columnName,
                                                       @RequestParam(value = "oldType", required = false) String oldType,
                                                       @RequestParam(value = "newType", required = false) String newType,
                                                       @RequestParam(value = "sourceFile", required = false) String sourceFile) throws Exception {
        return analysisJobService.createUploadJob(
                file,
                projectName,
                changeType,
                tableName,
                columnName,
                oldType,
                newType,
                sourceFile
        );
    }

    @GetMapping("/{jobId}")
    public AnalysisJobStatusResponse getJob(@PathVariable("jobId") String jobId) {
        return analysisJobService.getJob(jobId);
    }
}