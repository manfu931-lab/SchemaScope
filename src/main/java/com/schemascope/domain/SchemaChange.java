package com.schemascope.domain;

public class SchemaChange {

    private String changeId;
    private ChangeType changeType;
    private String tableName;
    private String columnName;
    private String oldType;
    private String newType;
    private boolean breaking;
    private String sourceFile;

    public SchemaChange() {
    }

    public SchemaChange(String changeId,
                        ChangeType changeType,
                        String tableName,
                        String columnName,
                        String oldType,
                        String newType,
                        boolean breaking,
                        String sourceFile) {
        this.changeId = changeId;
        this.changeType = changeType;
        this.tableName = tableName;
        this.columnName = columnName;
        this.oldType = oldType;
        this.newType = newType;
        this.breaking = breaking;
        this.sourceFile = sourceFile;
    }

    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
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

    public boolean isBreaking() {
        return breaking;
    }

    public void setBreaking(boolean breaking) {
        this.breaking = breaking;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public String toString() {
        return "SchemaChange{" +
                "changeId='" + changeId + '\'' +
                ", changeType=" + changeType +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", oldType='" + oldType + '\'' +
                ", newType='" + newType + '\'' +
                ", breaking=" + breaking +
                ", sourceFile='" + sourceFile + '\'' +
                '}';
    }
}

/*
这个类表示“一次数据库变更”。

每个字段的意思
changeId：这次变更的唯一编号
changeType：变更类型，比如删列、加列
tableName：表名
columnName：列名
oldType：旧类型
newType：新类型
breaking：是不是破坏性变更
sourceFile：这个变更来自哪个 migration 文件
*/