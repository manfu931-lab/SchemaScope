package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class SchemaTable {

    private String name;
    private List<SchemaColumn> columns = new ArrayList<>();

    public SchemaTable() {
    }

    public SchemaTable(String name, List<SchemaColumn> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SchemaColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<SchemaColumn> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "SchemaTable{" +
                "name='" + name + '\'' +
                ", columns=" + columns +
                '}';
    }
}