package com.schemascope.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PresentationAssetJsonExporter {

    private final ObjectMapper objectMapper;

    public PresentationAssetJsonExporter() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path export(Object asset, Path outputFile) throws IOException {
        if (asset == null) {
            throw new IllegalArgumentException("asset must not be null");
        }
        if (outputFile == null) {
            throw new IllegalArgumentException("outputFile must not be null");
        }

        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        objectMapper.writeValue(outputFile.toFile(), asset);
        return outputFile;
    }
}