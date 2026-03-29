package com.schemascope.parser;

import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        try (var pathStream = Files.walk(sourceRoot)) {
            pathStream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            JavaComponentType type = detectComponentType(path);
                            if (type != null) {
                                components.add(new JavaComponent(
                                        extractClassName(path),
                                        path.toString(),
                                        type
                                ));
                            }
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        return new JavaProjectScanResult(components);
    }

    private JavaComponentType detectComponentType(Path javaFile) throws IOException {
        List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);

        List<String> classAnnotations = new ArrayList<>();
        StringBuilder declarationBuffer = null;
        boolean collectingDeclaration = false;

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

            if (!collectingDeclaration && line.startsWith("@")) {
                classAnnotations.add(line);
                continue;
            }

            if (!collectingDeclaration && looksLikeTypeDeclarationStart(line)) {
                collectingDeclaration = true;
                declarationBuffer = new StringBuilder(line);

                if (isDeclarationComplete(line)) {
                    return resolveType(classAnnotations, declarationBuffer.toString());
                }
                continue;
            }

            if (collectingDeclaration) {
                declarationBuffer.append(' ').append(line);

                if (isDeclarationComplete(line)) {
                    return resolveType(classAnnotations, declarationBuffer.toString());
                }
                continue;
            }

            if (!classAnnotations.isEmpty()) {
                classAnnotations.clear();
            }
        }

        return null;
    }

    private boolean looksLikeTypeDeclarationStart(String line) {
        return line.contains(" class ")
                || line.startsWith("class ")
                || line.contains(" interface ")
                || line.startsWith("interface ")
                || line.contains(" record ")
                || line.startsWith("record ")
                || line.contains(" enum ")
                || line.startsWith("enum ")
                || hasVisibilityPrefixBeforeTypeKeyword(line);
    }

    private boolean hasVisibilityPrefixBeforeTypeKeyword(String line) {
        String normalized = line.toLowerCase();
        return (normalized.startsWith("public ")
                || normalized.startsWith("protected ")
                || normalized.startsWith("private ")
                || normalized.startsWith("abstract ")
                || normalized.startsWith("final "))
                && (normalized.contains(" class ")
                || normalized.contains(" interface ")
                || normalized.contains(" record ")
                || normalized.contains(" enum "));
    }

    private boolean isDeclarationComplete(String line) {
        return line.endsWith("{") || line.endsWith(";");
    }

    private JavaComponentType resolveType(List<String> annotations, String declaration) {
        JavaComponentType annotationType = resolveTypeFromAnnotations(annotations);
        if (annotationType != null) {
            return annotationType;
        }

        if (isRepositoryLikeDeclaration(declaration)) {
            return JavaComponentType.REPOSITORY;
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

    private boolean isRepositoryLikeDeclaration(String declaration) {
        String normalized = declaration.replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        if (!(normalized.contains(" interface ") || normalized.startsWith("interface ")
                || normalized.contains(" class ") || normalized.startsWith("class "))) {
            return false;
        }

        if (!normalized.contains(" extends ")) {
            return false;
        }

        return containsRepositorySuperType(normalized, "Repository")
                || containsRepositorySuperType(normalized, "CrudRepository")
                || containsRepositorySuperType(normalized, "ListCrudRepository")
                || containsRepositorySuperType(normalized, "PagingAndSortingRepository")
                || containsRepositorySuperType(normalized, "ListPagingAndSortingRepository")
                || containsRepositorySuperType(normalized, "JpaRepository")
                || containsRepositorySuperType(normalized, "JpaSpecificationExecutor")
                || containsRepositorySuperType(normalized, "MongoRepository")
                || containsRepositorySuperType(normalized, "ElasticsearchRepository")
                || containsRepositorySuperType(normalized, "ReactiveCrudRepository")
                || containsRepositorySuperType(normalized, "R2dbcRepository");
    }

    private boolean containsRepositorySuperType(String declaration, String typeName) {
        return declaration.contains("extends " + typeName + "<")
                || declaration.contains(", " + typeName + "<")
                || declaration.contains("extends " + typeName + " ")
                || declaration.contains(", " + typeName + " ");
    }

    private boolean matchesAnnotation(String line, String annotationName) {
        return line.equals("@" + annotationName)
                || line.startsWith("@" + annotationName + "(")
                || line.equals("@org.springframework.stereotype." + annotationName)
                || line.startsWith("@org.springframework.stereotype." + annotationName + "(")
                || line.equals("@jakarta.persistence." + annotationName)
                || line.startsWith("@jakarta.persistence." + annotationName + "(")
                || line.equals("@javax.persistence." + annotationName)
                || line.startsWith("@javax.persistence." + annotationName + "(");
    }

    private String extractClassName(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }
}