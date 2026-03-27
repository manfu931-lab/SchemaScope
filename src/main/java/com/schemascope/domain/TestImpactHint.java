package com.schemascope.domain;

public class TestImpactHint {

    private String testClassName;
    private String filePath;
    private String reason;
    private RiskLevel priority;

    public TestImpactHint() {
    }

    public TestImpactHint(String testClassName,
                          String filePath,
                          String reason,
                          RiskLevel priority) {
        this.testClassName = testClassName;
        this.filePath = filePath;
        this.reason = reason;
        this.priority = priority;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RiskLevel getPriority() {
        return priority;
    }

    public void setPriority(RiskLevel priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "TestImpactHint{" +
                "testClassName='" + testClassName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", reason='" + reason + '\'' +
                ", priority=" + priority +
                '}';
    }
}