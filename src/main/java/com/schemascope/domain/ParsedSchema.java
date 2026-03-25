package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class ParsedSchema {

    private List<SchemaTable> tables = new ArrayList<>();

    public ParsedSchema() {
    }

    public ParsedSchema(List<SchemaTable> tables) {
        this.tables = tables;
    }

    public List<SchemaTable> getTables() {
        return tables;
    }

    public void setTables(List<SchemaTable> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "ParsedSchema{" +
                "tables=" + tables +
                '}';
    }
}