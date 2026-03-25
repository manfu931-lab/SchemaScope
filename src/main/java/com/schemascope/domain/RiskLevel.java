package com.schemascope.domain;
/*
定义风险等级只有 3 种：

HIGH
MEDIUM
LOW

先别搞太复杂。
你后面风险评分模块成熟以后，再决定要不要加 CRITICAL */
public enum RiskLevel {
    HIGH,
    MEDIUM,
    LOW
}