package com.schemascope.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "schemascope.workspace")
public class WorkspaceProperties {

    private String rootDir = "./data/workspaces";

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }
}