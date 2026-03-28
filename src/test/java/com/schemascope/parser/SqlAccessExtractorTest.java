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
    void shouldExtractSqlAccessPointsFromJpaQueryJdbcTemplateAndMyBatisXml() throws Exception {
        SqlAccessExtractor extractor = new SqlAccessExtractor(new MyBatisXmlSqlExtractor());

        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        assertTrue(Files.exists(projectRoot), "Fixture project root not found: " + projectRoot);
        assertTrue(Files.exists(projectRoot.resolve("src/main/java/com/example/demo/OwnerRepository.java")),
                "Missing fixture file: OwnerRepository.java");
        assertTrue(Files.exists(projectRoot.resolve("src/main/java/com/example/demo/OwnerJdbcDao.java")),
                "Missing fixture file: OwnerJdbcDao.java");
        assertTrue(Files.exists(projectRoot.resolve("src/main/java/com/example/demo/VisitJdbcDao.java")),
                "Missing fixture file: VisitJdbcDao.java");
        assertTrue(Files.exists(projectRoot.resolve("src/main/resources/mapper/VisitMapper.xml")),
                "Missing fixture file: VisitMapper.xml");

        List<SqlAccessPoint> results = extractor.extractFromProject(projectRoot.toString());

        System.out.println("SqlAccessExtractor results = " + results);

        assertTrue(results.size() >= 7, "Expected at least 7 access points, actual: " + results.size());

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

        boolean hasVariableSqlVisitQuery = results.stream().anyMatch(point ->
                "VisitJdbcDao".equals(point.getOwnerClassName())
                        && "findVisitsByPetId".equals(point.getOwnerMethodName())
                        && point.getSourceType() == SqlSourceType.JDBC_TEMPLATE
                        && point.getReferencedTables().contains("visits")
                        && point.getNormalizedTokens().contains("pet_id")
        );

        boolean hasStringBuilderVisitQuery = results.stream().anyMatch(point ->
                "VisitJdbcDao".equals(point.getOwnerClassName())
                        && "findVisitDates".equals(point.getOwnerMethodName())
                        && point.getSourceType() == SqlSourceType.JDBC_TEMPLATE
                        && point.getReferencedTables().contains("visits")
                        && point.getNormalizedTokens().contains("visit_date")
        );

        boolean hasMyBatisSelect = results.stream().anyMatch(point ->
                "VisitMapper".equals(point.getOwnerClassName())
                        && "findVisitById".equals(point.getOwnerMethodName())
                        && point.getSourceType() == SqlSourceType.MYBATIS_XML
                        && point.getReferencedTables().contains("visits")
                        && point.getNormalizedTokens().contains("visit_date")
        );

        boolean hasMyBatisUpdate = results.stream().anyMatch(point ->
                "VisitMapper".equals(point.getOwnerClassName())
                        && "updateVisitDescription".equals(point.getOwnerMethodName())
                        && point.getSourceType() == SqlSourceType.MYBATIS_XML
                        && point.getReferencedTables().contains("visits")
                        && point.getNormalizedTokens().contains("description")
        );
        boolean hasQuotedOwnerQuery = results.stream().anyMatch(point ->
                "OwnerRepository".equals(point.getOwnerClassName())
                        && "findQuotedOwnerName".equals(point.getOwnerMethodName())
                        && point.getSourceType() == SqlSourceType.NATIVE_QUERY
                        && point.getReferencedTables().contains("owners")
                        && point.getNormalizedTokens().contains("last_name")
        );
        
        assertTrue(hasQuotedOwnerQuery, "Missing quoted-identifier native query access point. results=" + results);
        assertTrue(hasNativeOwnerQuery, "Missing native @Query access point. results=" + results);
        assertTrue(hasJdbcOwnerUpdate, "Missing jdbcTemplate direct-string access point. results=" + results);
        assertTrue(hasVariableSqlVisitQuery, "Missing variable-based jdbcTemplate SQL access point. results=" + results);
        assertTrue(hasStringBuilderVisitQuery, "Missing StringBuilder-based jdbcTemplate SQL access point. results=" + results);
        assertTrue(hasMyBatisSelect, "Missing MyBatis XML select access point. results=" + results);
        assertTrue(hasMyBatisUpdate, "Missing MyBatis XML update access point. results=" + results);
    }
}