package com.schemascope.service;

import com.schemascope.api.dto.AnalysisJobStatusResponse;
import com.schemascope.api.dto.CreateAnalysisJobResponse;
import com.schemascope.domain.job.AnalysisJob;
import com.schemascope.domain.job.AnalysisJobStatus;
import com.schemascope.domain.job.AnalysisSourceType;
import com.schemascope.repository.AnalysisJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class AnalysisJobService {

    private final AnalysisJobRepository analysisJobRepository;
    private final WorkspaceService workspaceService;

    public AnalysisJobService(AnalysisJobRepository analysisJobRepository,
                              WorkspaceService workspaceService) {
        this.analysisJobRepository = analysisJobRepository;
        this.workspaceService = workspaceService;
    }

    @Transactional
    public CreateAnalysisJobResponse createUploadJob(MultipartFile file,
                                                     String projectName,
                                                     String changeType,
                                                     String tableName,
                                                     String columnName,
                                                     String oldType,
                                                     String newType,
                                                     String sourceFile) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded zip file must not be empty");
        }

        AnalysisJob job = new AnalysisJob();
        job.setStatus(AnalysisJobStatus.PENDING);
        job.setSourceType(AnalysisSourceType.ZIP_UPLOAD);
        job.setProjectName(projectName);
        job.setChangeType(changeType);
        job.setTableName(tableName);
        job.setColumnName(columnName);
        job.setOldType(oldType);
        job.setNewType(newType);
        job.setSourceFile(sourceFile);
        job.setUploadFileName(file.getOriginalFilename());

        job = analysisJobRepository.save(job);

        Path workspaceDir = workspaceService.createJobWorkspace(job.getId());
        Path uploadedZip = workspaceService.saveUploadedZip(job.getId(), file);

        job.setWorkspaceDir(workspaceDir.toString());
        job.setUploadedZipPath(uploadedZip.toString());
        job.setStatus(AnalysisJobStatus.UPLOADED);

        analysisJobRepository.save(job);

        return new CreateAnalysisJobResponse(
                job.getId(),
                job.getStatus(),
                "Analysis job created successfully"
        );
    }

    @Transactional(readOnly = true)
    public AnalysisJobStatusResponse getJob(String jobId) {
        AnalysisJob job = analysisJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis job not found: " + jobId));

        AnalysisJobStatusResponse response = new AnalysisJobStatusResponse();
        response.setJobId(job.getId());
        response.setStatus(job.getStatus());
        response.setProjectName(job.getProjectName());
        response.setChangeType(job.getChangeType());
        response.setTableName(job.getTableName());
        response.setColumnName(job.getColumnName());
        response.setUploadFileName(job.getUploadFileName());
        response.setErrorMessage(job.getErrorMessage());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());

        return response;
    }
}