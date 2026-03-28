package com.schemascope.benchmark;

import java.nio.file.Path;
import java.util.List;

public class BenchmarkProjectCatalog {

    public List<BenchmarkProjectSpec> loadAll() {
        return List.of(
                buildSqlDemoProject(),
                buildOrderDemoProject()
        );
    }

    private BenchmarkProjectSpec buildSqlDemoProject() {
        return new BenchmarkProjectSpec(
                "sql-demo-project",
                Path.of("src", "test", "resources", "fixture", "sql-demo-project").toAbsolutePath().normalize(),
                Path.of("src", "test", "resources", "benchmark", "sql-demo-schema", "schema_v1.sql").toAbsolutePath().normalize(),
                Path.of("src", "test", "resources", "benchmark", "sql-demo-schema", "schema_v2.sql").toAbsolutePath().normalize(),
                "owners",
                "last_name",
                BenchmarkProjectSpec.linkedSet(
                        "OwnerJdbcDao",
                        "OwnerRepository",
                        "OwnerService",
                        "OwnerController"
                ),
                BenchmarkProjectSpec.linkedSet(
                        "OwnerJdbcDao",
                        "OwnerRepository",
                        "OwnerService"
                ),
                BenchmarkProjectSpec.linkedSet("OwnerController")
        );
    }

    private BenchmarkProjectSpec buildOrderDemoProject() {
        return new BenchmarkProjectSpec(
                "order-demo-project",
                Path.of("src", "test", "resources", "fixture", "order-demo-project").toAbsolutePath().normalize(),
                Path.of("src", "test", "resources", "benchmark", "order-demo-schema", "schema_v1.sql").toAbsolutePath().normalize(),
                Path.of("src", "test", "resources", "benchmark", "order-demo-schema", "schema_v2.sql").toAbsolutePath().normalize(),
                "orders",
                "status",
                BenchmarkProjectSpec.linkedSet(
                        "OrderJdbcDao",
                        "OrderRepository",
                        "OrderService",
                        "OrderController"
                ),
                BenchmarkProjectSpec.linkedSet(
                        "OrderJdbcDao",
                        "OrderRepository",
                        "OrderService"
                ),
                BenchmarkProjectSpec.linkedSet("OrderController")
        );
    }
}