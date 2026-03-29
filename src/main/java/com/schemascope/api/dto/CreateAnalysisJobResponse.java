package com.schemascope.api.dto;

import com.schemascope.domain.job.AnalysisJobStatus;

public class CreateAnalysisJobResponse {

    private String jobId;
    private AnalysisJobStatus status;
    private String message;

    public CreateAnalysisJobResponse() {
    }

    public CreateAnalysisJobResponse(String jobId, AnalysisJobStatus status, String message) {
        this.jobId = jobId;
        this.status = status;
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public AnalysisJobStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}