package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class SqlAccessPoint {

    private String sqlId;
    private String ownerClassName;
    private String ownerMethodName;
    private String sourceFile;
    private String rawSql;
    private SqlSourceType sourceType;
    private List<String> referencedTables = new ArrayList<>();
    private List<String> normalizedTokens = new ArrayList<>();

    public SqlAccessPoint() {
    }

    public SqlAccessPoint(String sqlId,
                          String ownerClassName,
                          String ownerMethodName,
                          String sourceFile,
                          String rawSql,
                          SqlSourceType sourceType,
                          List<String> referencedTables,
                          List<String> normalizedTokens) {
        this.sqlId = sqlId;
        this.ownerClassName = ownerClassName;
        this.ownerMethodName = ownerMethodName;
        this.sourceFile = sourceFile;
        this.rawSql = rawSql;
        this.sourceType = sourceType;
        this.referencedTables = referencedTables;
        this.normalizedTokens = normalizedTokens;
    }

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getOwnerClassName() {
        return ownerClassName;
    }

    public void setOwnerClassName(String ownerClassName) {
        this.ownerClassName = ownerClassName;
    }

    public String getOwnerMethodName() {
        return ownerMethodName;
    }

    public void setOwnerMethodName(String ownerMethodName) {
        this.ownerMethodName = ownerMethodName;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getRawSql() {
        return rawSql;
    }

    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }

    public SqlSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SqlSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public List<String> getReferencedTables() {
        return referencedTables;
    }

    public void setReferencedTables(List<String> referencedTables) {
        this.referencedTables = referencedTables;
    }

    public List<String> getNormalizedTokens() {
        return normalizedTokens;
    }

    public void setNormalizedTokens(List<String> normalizedTokens) {
        this.normalizedTokens = normalizedTokens;
    }

    @Override
    public String toString() {
        return "SqlAccessPoint{" +
                "sqlId='" + sqlId + '\'' +
                ", ownerClassName='" + ownerClassName + '\'' +
                ", ownerMethodName='" + ownerMethodName + '\'' +
                ", sourceFile='" + sourceFile + '\'' +
                ", rawSql='" + rawSql + '\'' +
                ", sourceType=" + sourceType +
                ", referencedTables=" + referencedTables +
                ", normalizedTokens=" + normalizedTokens +
                '}';
    }
}