package com.schemascope.domain;

public class SchemaColumn {

    private String name;
    private String definition;

    public SchemaColumn() {
    }

    public SchemaColumn(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "SchemaColumn{" +
                "name='" + name + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}