package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class JavaProjectScanResult {

    private List<JavaComponent> components = new ArrayList<>();

    public JavaProjectScanResult() {
    }

    public JavaProjectScanResult(List<JavaComponent> components) {
        this.components = components;
    }

    public List<JavaComponent> getComponents() {
        return components;
    }

    public void setComponents(List<JavaComponent> components) {
        this.components = components;
    }

    public long countByType(JavaComponentType type) {
        return components.stream()
                .filter(component -> component.getComponentType() == type)
                .count();
    }

    @Override
    public String toString() {
        return "JavaProjectScanResult{" +
                "components=" + components +
                '}';
    }
}