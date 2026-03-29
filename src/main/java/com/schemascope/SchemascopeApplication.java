package com.schemascope;

import com.schemascope.config.WorkspaceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WorkspaceProperties.class)
public class SchemascopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchemascopeApplication.class, args);
    }
}