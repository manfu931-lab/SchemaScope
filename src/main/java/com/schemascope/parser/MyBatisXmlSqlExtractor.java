package com.schemascope.parser;

import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlSourceType;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MyBatisXmlSqlExtractor {

    private static final Pattern TABLE_PATTERN =
            Pattern.compile("(?i)\\b(from|join|update|into)\\s+[`\"]?([a-zA-Z0-9_]+)[`\"]?");

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[a-z0-9_]+");

    public List<SqlAccessPoint> extractFromXmlFile(Path xmlFile)
            throws IOException, ParserConfigurationException, SAXException {
        List<SqlAccessPoint> results = new ArrayList<>();

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(xmlFile.toFile());

        Element root = document.getDocumentElement();
        if (root == null || !"mapper".equals(root.getTagName())) {
            return results;
        }

        String namespace = root.getAttribute("namespace");
        String ownerClassName = toSimpleName(namespace);
        if (ownerClassName == null || ownerClassName.isBlank()) {
            ownerClassName = xmlFile.getFileName().toString().replace(".xml", "");
        }

        collectStatements(root, "select", ownerClassName, xmlFile, results);
        collectStatements(root, "update", ownerClassName, xmlFile, results);
        collectStatements(root, "insert", ownerClassName, xmlFile, results);
        collectStatements(root, "delete", ownerClassName, xmlFile, results);

        return results;
    }

    private void collectStatements(Element root,
                                   String tagName,
                                   String ownerClassName,
                                   Path xmlFile,
                                   List<SqlAccessPoint> results) {
        NodeList nodeList = root.getElementsByTagName(tagName);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }

            String statementId = element.getAttribute("id");
            if (statementId == null || statementId.isBlank()) {
                continue;
            }

            String rawSql = normalizeSql(collectTextContent(element));
            if (rawSql.isBlank()) {
                continue;
            }

            SqlAccessPoint accessPoint = new SqlAccessPoint();
            accessPoint.setSqlId(ownerClassName + "#" + statementId + "#" + SqlSourceType.MYBATIS_XML);
            accessPoint.setOwnerClassName(ownerClassName);
            accessPoint.setOwnerMethodName(statementId);
            accessPoint.setSourceFile(xmlFile.toString());
            accessPoint.setRawSql(rawSql);
            accessPoint.setSourceType(SqlSourceType.MYBATIS_XML);
            accessPoint.setReferencedTables(extractTables(rawSql));
            accessPoint.setNormalizedTokens(extractNormalizedTokens(rawSql));

            results.add(accessPoint);
        }
    }

    private String collectTextContent(Node node) {
        StringBuilder sb = new StringBuilder();
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(child.getTextContent()).append(' ');
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                sb.append(collectTextContent(child)).append(' ');
            }
        }

        return sb.toString();
    }

    private String normalizeSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    private String toSimpleName(String fqcn) {
        if (fqcn == null || fqcn.isBlank()) {
            return null;
        }

        int idx = fqcn.lastIndexOf('.');
        if (idx < 0 || idx >= fqcn.length() - 1) {
            return fqcn;
        }
        return fqcn.substring(idx + 1);
    }

    private List<String> extractTables(String sql) {
        Set<String> tables = new LinkedHashSet<>();
        Matcher matcher = TABLE_PATTERN.matcher(sql);
        while (matcher.find()) {
            tables.add(matcher.group(2).toLowerCase());
        }
        return new ArrayList<>(tables);
    }

    private List<String> extractNormalizedTokens(String sql) {
        Set<String> tokens = new LinkedHashSet<>();
        Matcher matcher = TOKEN_PATTERN.matcher(sql.toLowerCase());
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() < 2) {
                continue;
            }
            tokens.add(token);
            if (token.contains("_")) {
                String compact = token.replace("_", "");
                if (compact.length() >= 2) {
                    tokens.add(compact);
                }
            }
        }
        return new ArrayList<>(tokens);
    }
}