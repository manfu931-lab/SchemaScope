package com.schemascope.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.schemascope.benchmark.view.BenchmarkDashboardView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BenchmarkDashboardJsonExporter {

    private final ObjectMapper objectMapper;

    public BenchmarkDashboardJsonExporter() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path export(BenchmarkDashboardView dashboardView, Path outputFile) throws IOException {
        if (dashboardView == null) {
            throw new IllegalArgumentException("dashboardView must not be null");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("outputFile must not be null");
        }

        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        objectMapper.writeValue(outputFile.toFile(), dashboardView);
        return outputFile;
    }
}