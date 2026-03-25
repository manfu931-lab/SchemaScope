package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SimpleImpactAnalyzer {

    public List<ImpactResult> analyze(SchemaChange change) {
        List<ImpactResult> results = new ArrayList<>();

        if (change.getChangeType() == null) {
            return results;
        }

        if (change.getChangeType() == ChangeType.DROP_COLUMN) {
            results.add(new ImpactResult(
                    change.getChangeId(),
                    "/api/orders/list",
                    "API",
                    87.5,
                    RiskLevel.HIGH,
                    0.91,
                    Arrays.asList(
                            change.getTableName() + "." + change.getColumnName(),
                            "OrderRepository.queryByStatus",
                            "OrderService.listOrders",
                            "/api/orders/list"
                    )
            ));

            results.add(new ImpactResult(
                    change.getChangeId(),
                    "OrderService.listOrders",
                    "METHOD",
                    72.0,
                    RiskLevel.MEDIUM,
                    0.86,
                    Arrays.asList(
                            change.getTableName() + "." + change.getColumnName(),
                            "OrderRepository.queryByStatus",
                            "OrderService.listOrders"
                    )
            ));
        }

        if (change.getChangeType() == ChangeType.ALTER_COLUMN_TYPE) {
            results.add(new ImpactResult(
                change.getChangeId(),
                "UserRepository.updateEmail",
                "METHOD",
                78.0,
                RiskLevel.HIGH,
                0.89,
                Arrays.asList(
                        change.getTableName() + "." + change.getColumnName(),
                        change.getOldType() + " -> " + change.getNewType(),
                        "UserRepository.updateEmail",
                        "UserProfileService.updateEmail"
                )
        ));

        results.add(new ImpactResult(
            change.getChangeId(),
            "/api/users/profile/update",
            "API",
            69.5,
            RiskLevel.MEDIUM,
            0.83,
            Arrays.asList(
                    change.getTableName() + "." + change.getColumnName(),
                    change.getOldType() + " -> " + change.getNewType(),
                    "UserProfileService.updateEmail",
                    "/api/users/profile/update"
            )
    ));
        }

        return results;
    }
}