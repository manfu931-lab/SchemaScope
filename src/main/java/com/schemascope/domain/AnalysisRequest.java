package com.schemascope.domain;
/*
定义“用户发起一次分析时，需要告诉系统什么”。

在第一版里，我们先不要做太复杂。
先假设用户是通过接口发来这些信息：

项目名称
项目路径
旧 schema 文件路径
新 schema 文件路径 */
public class AnalysisRequest {

    private String projectName;
    private String projectPath;
    private String oldSchemaPath;
    private String newSchemaPath;

    public AnalysisRequest() {
    }

    public AnalysisRequest(String projectName, String projectPath, String oldSchemaPath, String newSchemaPath) {
        this.projectName = projectName;
        this.projectPath = projectPath;
        this.oldSchemaPath = oldSchemaPath;
        this.newSchemaPath = newSchemaPath;
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

    @Override
    public String toString() {
        return "AnalysisRequest{" +
                "projectName='" + projectName + '\'' +
                ", projectPath='" + projectPath + '\'' +
                ", oldSchemaPath='" + oldSchemaPath + '\'' +
                ", newSchemaPath='" + newSchemaPath + '\'' +
                '}';
    }
}