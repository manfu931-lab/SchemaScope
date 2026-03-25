api 放控制器，也就是前端或浏览器访问的接口入口。



config 放配置类，比如以后跨域、Swagger、路径配置。



domain 放核心数据对象，比如：

SchemaChange

CodeEntity

ImpactResult





service 放业务逻辑，比如：

项目分析服务

风险评分服务



repository 这里不是数据库 Repository 的意思，先给平台自己以后用的数据访问层预留。



parser 放 Java 项目解析相关内容。



schemadiff 放数据库 schema 变化识别模块。



graph 放依赖图和影响传播图。



risk 放风险评分模型。



review 放 PR 审查和报告生成。



common 放通用工具类、枚举、常量。

