package com.schemascope.parser;

import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpringProjectScanner {

    public JavaProjectScanResult scan(String projectRootPath) throws IOException {
        List<JavaComponent> components = new ArrayList<>();

        Path sourceRoot = Path.of(projectRootPath, "src", "main", "java");
        if (!Files.exists(sourceRoot)) {
            sourceRoot = Path.of(projectRootPath);
        }

        Files.walk(sourceRoot)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        List<String> lines = Files.readAllLines(path);

                        JavaComponentType type = detectComponentType(lines);
                        if (type != null) {
                            String className = extractClassName(path);
                            components.add(new JavaComponent(
                                    className,
                                    path.toString(),
                                    type
                            ));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        return new JavaProjectScanResult(components);
    }

    private JavaComponentType detectComponentType(List<String> lines) {
        List<String> classAnnotations = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()
                    || line.startsWith("package ")
                    || line.startsWith("import ")
                    || line.startsWith("//")
                    || line.startsWith("*")
                    || line.startsWith("/*")
                    || line.startsWith("*/")) {
                continue;
            }

            if (line.startsWith("@")) {
                classAnnotations.add(line);
                continue;
            }

            if (line.contains(" class ")
                    || line.startsWith("class ")
                    || line.contains(" interface ")
                    || line.startsWith("interface ")
                    || line.contains(" record ")
                    || line.startsWith("record ")) {
                return resolveTypeFromAnnotations(classAnnotations);
            }

            if (!classAnnotations.isEmpty()) {
                classAnnotations.clear();
            }
        }

        return null;
    }

    private JavaComponentType resolveTypeFromAnnotations(List<String> annotations) {
        for (String annotation : annotations) {
            if (matchesAnnotation(annotation, "RestController")) {
                return JavaComponentType.REST_CONTROLLER;
            }
            if (matchesAnnotation(annotation, "Controller")) {
                return JavaComponentType.CONTROLLER;
            }
            if (matchesAnnotation(annotation, "Service")) {
                return JavaComponentType.SERVICE;
            }
            if (matchesAnnotation(annotation, "Repository")) {
                return JavaComponentType.REPOSITORY;
            }
            if (matchesAnnotation(annotation, "Entity")) {
                return JavaComponentType.ENTITY;
            }
        }
        return null;
    }
    private boolean matchesAnnotation(String line, String annotationName) {
        return line.equals("@" + annotationName)
                || line.startsWith("@" + annotationName + "(");
    }

    private String extractClassName(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }
}