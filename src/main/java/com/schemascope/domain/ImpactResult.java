package com.schemascope.domain;
/*
定义“系统分析出的一条结果”长什么样。

比如以后系统分析出：

这次删除 orders.status
影响到了 /api/orders/list
风险等级高
风险分数 87.5
证据链是：列 → SQL → Repository → Service → API

这些信息就要装在 ImpactResult 里。 */
import java.util.List;

public class ImpactResult {

    private String changeId;
    private String affectedObject;
    private String affectedType;
    private double riskScore;
    private RiskLevel riskLevel;
    private double confidence;
    private List<String> evidencePath;

    public ImpactResult() {
    }
/*changeId：对应哪次 schema 变化
affectedObject：受影响对象是谁，比如某个 API 或方法
affectedType：受影响对象类型，比如 API、METHOD
riskScore：风险分数
riskLevel：高/中/低风险
confidence：分析结果置信度
evidencePath：证据链路径 */
    public ImpactResult(String changeId,            
                        String affectedObject,
                        String affectedType,
                        double riskScore,
                        RiskLevel riskLevel,
                        double confidence,
                        List<String> evidencePath) {
        this.changeId = changeId;
        this.affectedObject = affectedObject;
        this.affectedType = affectedType;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.evidencePath = evidencePath;
    }

    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }

    public String getAffectedObject() {
        return affectedObject;
    }

    public void setAffectedObject(String affectedObject) {
        this.affectedObject = affectedObject;
    }

    public String getAffectedType() {
        return affectedType;
    }

    public void setAffectedType(String affectedType) {
        this.affectedType = affectedType;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(List<String> evidencePath) {
        this.evidencePath = evidencePath;
    }

    @Override
    public String toString() {
        return "ImpactResult{" +
                "changeId='" + changeId + '\'' +
                ", affectedObject='" + affectedObject + '\'' +
                ", affectedType='" + affectedType + '\'' +
                ", riskScore=" + riskScore +
                ", riskLevel=" + riskLevel +
                ", confidence=" + confidence +
                ", evidencePath=" + evidencePath +
                '}';
    }
}