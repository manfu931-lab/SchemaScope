package com.schemascope.parser;

import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlSourceType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlAccessExtractorTest {

    @Test
    void shouldExtractSqlAccessPointsFromJpaQueryAndJdbcTemplate() throws Exception {
        SqlAccessExtractor extractor = new SqlAccessExtractor();

        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        assertTrue(Files.exists(projectRoot), "Fixture project root not found: " + projectRoot);
        assertTrue(Files.exists(projectRoot.resolve("src/main/java/com/example/demo/OwnerRepository.java")),
                "Missing fixture file: OwnerRepository.java");
        assertTrue(Files.exists(projectRoot.resolve("src/main/java/com/example/demo/OwnerJdbcDao.java")),
                "Missing fixture file: OwnerJdbcDao.java");

        List<SqlAccessPoint> results = extractor.extractFromProject(projectRoot.toString());

        System.out.println("SqlAccessExtractor results = " + results);

        assertTrue(results.size() >= 2, "Expected at least 2 access points, actual: " + results.size());

        boolean hasNativeOwnerQuery = results.stream().anyMatch(point ->
                "OwnerRepository".equals(point.getOwnerClassName())
                        && "findByLastName".equals(point.getOwnerMethodName())
                        && point.getSourceType() == SqlSourceType.NATIVE_QUERY
                        && point.getReferencedTables().contains("owners")
                        && point.getNormalizedTokens().contains("last_name")
        );

        boolean hasJdbcOwnerUpdate = results.stream().anyMatch(point ->
                "OwnerJdbcDao".equals(point.getOwnerClassName())
                        && "updateOwnerLastName".equals(point.getOwnerMethodName())
                        && point.getSourceType() == SqlSourceType.JDBC_TEMPLATE
                        && point.getReferencedTables().contains("owners")
                        && point.getNormalizedTokens().contains("last_name")
        );

        assertTrue(hasNativeOwnerQuery, "Missing native @Query access point. results=" + results);
        assertTrue(hasJdbcOwnerUpdate, "Missing jdbcTemplate access point. results=" + results);
    }
}