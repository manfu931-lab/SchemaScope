package com.schemascope.domain;

public enum ChangeType {
    ADD_TABLE,
    DROP_TABLE,
    ADD_COLUMN,
    DROP_COLUMN,
    ALTER_COLUMN_TYPE,
    RENAME_COLUMN,
    ADD_INDEX,
    DROP_INDEX,
    ALTER_CONSTRAINT
}
/*
它定义了数据库结构变化的种类。

比如：

ADD_COLUMN：新增列
DROP_COLUMN：删除列
ALTER_COLUMN_TYPE：修改列类型
*/
