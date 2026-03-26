package com.schemascope.domain;
/*它表示一次扫描结果中的一个对象，比如：

HealthController
路径：src/main/java/.../HealthController.java
类型：REST_CONTROLLER */
public class JavaComponent {

    private String className;
    private String filePath;
    private JavaComponentType componentType;

    public JavaComponent() {
    }

    public JavaComponent(String className, String filePath, JavaComponentType componentType) {
        this.className = className;
        this.filePath = filePath;
        this.componentType = componentType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public JavaComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(JavaComponentType componentType) {
        this.componentType = componentType;
    }

    @Override
    public String toString() {
        return "JavaComponent{" +
                "className='" + className + '\'' +
                ", filePath='" + filePath + '\'' +
                ", componentType=" + componentType +
                '}';
    }
}