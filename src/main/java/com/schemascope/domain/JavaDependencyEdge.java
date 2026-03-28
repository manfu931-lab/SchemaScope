package com.schemascope.domain;

public class JavaDependencyEdge {

    private String dependencyClassName;
    private String dependentClassName;
    private String evidenceType;
    private String evidenceText;
    private String dependentMethodName;
    private String dependencyMethodName;

    public JavaDependencyEdge() {
    }

    public JavaDependencyEdge(String dependencyClassName,
                              String dependentClassName,
                              String evidenceType,
                              String evidenceText) {
        this(dependencyClassName, dependentClassName, evidenceType, evidenceText, null, null);
    }

    public JavaDependencyEdge(String dependencyClassName,
                              String dependentClassName,
                              String evidenceType,
                              String evidenceText,
                              String dependentMethodName,
                              String dependencyMethodName) {
        this.dependencyClassName = dependencyClassName;
        this.dependentClassName = dependentClassName;
        this.evidenceType = evidenceType;
        this.evidenceText = evidenceText;
        this.dependentMethodName = dependentMethodName;
        this.dependencyMethodName = dependencyMethodName;
    }

    public String getDependencyClassName() {
        return dependencyClassName;
    }

    public void setDependencyClassName(String dependencyClassName) {
        this.dependencyClassName = dependencyClassName;
    }

    public String getDependentClassName() {
        return dependentClassName;
    }

    public void setDependentClassName(String dependentClassName) {
        this.dependentClassName = dependentClassName;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getEvidenceText() {
        return evidenceText;
    }

    public void setEvidenceText(String evidenceText) {
        this.evidenceText = evidenceText;
    }

    public String getDependentMethodName() {
        return dependentMethodName;
    }

    public void setDependentMethodName(String dependentMethodName) {
        this.dependentMethodName = dependentMethodName;
    }

    public String getDependencyMethodName() {
        return dependencyMethodName;
    }

    public void setDependencyMethodName(String dependencyMethodName) {
        this.dependencyMethodName = dependencyMethodName;
    }

    @Override
    public String toString() {
        return "JavaDependencyEdge{" +
                "dependencyClassName='" + dependencyClassName + '\'' +
                ", dependentClassName='" + dependentClassName + '\'' +
                ", evidenceType='" + evidenceType + '\'' +
                ", evidenceText='" + evidenceText + '\'' +
                ", dependentMethodName='" + dependentMethodName + '\'' +
                ", dependencyMethodName='" + dependencyMethodName + '\'' +
                '}';
    }
}