package com.schemascope.service.impl;
/*
因为现在还没做真实的 Schema Diff 和代码分析，所以先做一个模拟版服务。

模拟版服务的意义是：

先把接口打通
先验证前后端交互格式
后面再把“模拟结果”替换成“真实分析结果” */
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.RiskLevel;
import com.schemascope.service.AnalysisService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MockAnalysisService implements AnalysisService {

    @Override
    public List<ImpactResult> analyze(AnalysisRequest request) {
        ImpactResult result1 = new ImpactResult(
                "chg-001",
                "/api/orders/list",
                "API",
                87.5,
                RiskLevel.HIGH,
                0.91,
                Arrays.asList(
                        "orders.status",
                        "OrderRepository.queryByStatus",
                        "OrderService.listOrders",
                        "/api/orders/list"
                )
        );

        ImpactResult result2 = new ImpactResult(
                "chg-001",
                "OrderService.listOrders",
                "METHOD",
                72.0,
                RiskLevel.MEDIUM,
                0.86,
                Arrays.asList(
                        "orders.status",
                        "OrderRepository.queryByStatus",
                        "OrderService.listOrders"
                )
        );

        return Arrays.asList(result1, result2);
    }
}