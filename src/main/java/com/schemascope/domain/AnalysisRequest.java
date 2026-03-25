package com.schemascope.domain;

public class AnalysisRequest {

    private String projectName;
    private String projectPath;
    private String oldSchemaPath;
    private String newSchemaPath;

    private String changeType;
    private String tableName;
    private String columnName;
    private String oldType;
    private String newType;
    private String sourceFile;

    public AnalysisRequest() {
    }

    public AnalysisRequest(String projectName,
                           String projectPath,
                           String oldSchemaPath,
                           String newSchemaPath,
                           String changeType,
                           String tableName,
                           String columnName,
                           String oldType,
                           String newType,
                           String sourceFile) {
        this.projectName = projectName;
        this.projectPath = projectPath;
        this.oldSchemaPath = oldSchemaPath;
        this.newSchemaPath = newSchemaPath;
        this.changeType = changeType;
        this.tableName = tableName;
        this.columnName = columnName;
        this.oldType = oldType;
        this.newType = newType;
        this.sourceFile = sourceFile;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getOldSchemaPath() {
        return oldSchemaPath;
    }

    public void setOldSchemaPath(String oldSchemaPath) {
        this.oldSchemaPath = oldSchemaPath;
    }

    public String getNewSchemaPath() {
        return newSchemaPath;
    }

    public void setNewSchemaPath(String newSchemaPath) {
        this.newSchemaPath = newSchemaPath;
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

    @Override
    public String toString() {
        return "AnalysisRequest{" +
                "projectName='" + projectName + '\'' +
                ", projectPath='" + projectPath + '\'' +
                ", oldSchemaPath='" + oldSchemaPath + '\'' +
                ", newSchemaPath='" + newSchemaPath + '\'' +
                ", changeType='" + changeType + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", oldType='" + oldType + '\'' +
                ", newType='" + newType + '\'' +
                ", sourceFile='" + sourceFile + '\'' +
                '}';
    }
}