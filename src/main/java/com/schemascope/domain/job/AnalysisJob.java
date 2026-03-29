package com.schemascope.domain.job;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_jobs")
public class AnalysisJob {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AnalysisJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private AnalysisSourceType sourceType;

    @Column(name = "project_name", length = 255)
    private String projectName;

    @Column(name = "change_type", length = 64)
    private String changeType;

    @Column(name = "table_name", length = 255)
    private String tableName;

    @Column(name = "column_name", length = 255)
    private String columnName;

    @Column(name = "old_type", length = 255)
    private String oldType;

    @Column(name = "new_type", length = 255)
    private String newType;

    @Column(name = "source_file", length = 255)
    private String sourceFile;

    @Column(name = "upload_file_name", length = 512)
    private String uploadFileName;

    @Column(name = "workspace_dir", length = 1024)
    private String workspaceDir;

    @Column(name = "uploaded_zip_path", length = 1024)
    private String uploadedZipPath;

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AnalysisJob() {
    }

    @PrePersist
    public void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public AnalysisJobStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisJobStatus status) {
        this.status = status;
    }

    public AnalysisSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(AnalysisSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getOldType() {
        return oldType;
    }

    public void setOldType(String oldType) {
        this.oldType = oldType;
    }

    public String getNewType() {
        return newType;
    }

    public void setNewType(String newType) {
        this.newType = newType;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(String workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public String getUploadedZipPath() {
        return uploadedZipPath;
    }

    public void setUploadedZipPath(String uploadedZipPath) {
        this.uploadedZipPath = uploadedZipPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}