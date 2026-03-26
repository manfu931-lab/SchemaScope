package com.schemascope.domain;

public enum JavaComponentType {
    REST_CONTROLLER,
    CONTROLLER,
    SERVICE,
    REPOSITORY,
    ENTITY
}

/*
它定义了扫描器当前支持识别的组件：

REST_CONTROLLER
SERVICE
REPOSITORY
ENTITY

以后你还可以继续加：

CONTROLLER
CONFIGURATION
TEST */
