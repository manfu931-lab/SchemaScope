package com.schemascope.domain;

public class SelectedTestCase {

    private String testClassName;
    private String filePath;
    private double score;
    private RiskLevel priority;
    private boolean existingTest;
    private String reason;

    public SelectedTestCase() {
    }

    public SelectedTestCase(String testClassName,
                            String filePath,
                            double score,
                            RiskLevel priority,
                            boolean existingTest,
                            String reason) {
        this.testClassName = testClassName;
        this.filePath = filePath;
        this.score = score;
        this.priority = priority;
        this.existingTest = existingTest;
        this.reason = reason;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public void setTestClassName(String testClassName) {
        this.testClassName = testClassName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public RiskLevel getPriority() {
        return priority;
    }

    public void setPriority(RiskLevel priority) {
        this.priority = priority;
    }

    public boolean isExistingTest() {
        return existingTest;
    }

    public void setExistingTest(boolean existingTest) {
        this.existingTest = existingTest;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "SelectedTestCase{" +
                "testClassName='" + testClassName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", score=" + score +
                ", priority=" + priority +
                ", existingTest=" + existingTest +
                ", reason='" + reason + '\'' +
                '}';
    }
}