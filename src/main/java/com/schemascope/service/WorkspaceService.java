package com.schemascope.service;

import com.schemascope.config.WorkspaceProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Service
public class WorkspaceService {

    private final WorkspaceProperties workspaceProperties;

    public WorkspaceService(WorkspaceProperties workspaceProperties) {
        this.workspaceProperties = workspaceProperties;
    }

    public Path createJobWorkspace(String jobId) throws IOException {
        Path workspaceRoot = Path.of(workspaceProperties.getRootDir()).toAbsolutePath().normalize();
        Files.createDirectories(workspaceRoot);

        Path jobWorkspace = workspaceRoot.resolve(jobId);
        Files.createDirectories(jobWorkspace);
        Files.createDirectories(jobWorkspace.resolve("upload"));
        Files.createDirectories(jobWorkspace.resolve("project"));
        Files.createDirectories(jobWorkspace.resolve("result"));

        return jobWorkspace;
    }

    public Path saveUploadedZip(String jobId, MultipartFile file) throws IOException {
        Path jobWorkspace = createJobWorkspace(jobId);
        Path uploadDir = jobWorkspace.resolve("upload");

        String originalFilename = file.getOriginalFilename() == null ? "project.zip" : file.getOriginalFilename();
        Path zipPath = uploadDir.resolve(originalFilename).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, zipPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return zipPath;
    }
}