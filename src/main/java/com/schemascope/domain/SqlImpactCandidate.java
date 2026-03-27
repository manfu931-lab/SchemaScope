package com.schemascope.domain;

public class SqlImpactCandidate {

    private SqlAccessPoint accessPoint;
    private double score;
    private String reason;
    private boolean tableMatched;
    private boolean columnMatched;

    public SqlImpactCandidate() {
    }

    public SqlImpactCandidate(SqlAccessPoint accessPoint,
                              double score,
                              String reason,
                              boolean tableMatched,
                              boolean columnMatched) {
        this.accessPoint = accessPoint;
        this.score = score;
        this.reason = reason;
        this.tableMatched = tableMatched;
        this.columnMatched = columnMatched;
    }

    public SqlAccessPoint getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(SqlAccessPoint accessPoint) {
        this.accessPoint = accessPoint;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isTableMatched() {
        return tableMatched;
    }

    public void setTableMatched(boolean tableMatched) {
        this.tableMatched = tableMatched;
    }

    public boolean isColumnMatched() {
        return columnMatched;
    }

    public void setColumnMatched(boolean columnMatched) {
        this.columnMatched = columnMatched;
    }

    @Override
    public String toString() {
        return "SqlImpactCandidate{" +
                "accessPoint=" + accessPoint +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                ", tableMatched=" + tableMatched +
                ", columnMatched=" + columnMatched +
                '}';
    }
}