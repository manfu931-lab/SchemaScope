SchemaFileReader.java 真正读取 .sql 文件，并解析出表和列
SpringProjectScanner.java 扫描 Spring Boot 项目源码目录，识别注解组件
SqlAccessExtractor.java 这是第一步最核心的类。它现在先支持两类SQ来源：
    @Query(...)
    jdbcTemplate.query/update/queryForObject(...)
