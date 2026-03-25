一、系统总体架构



先记住一句话：



SchemaScope 不是一个“数据库 diff 工具”，而是一个“数据库变更驱动的工程影响分析系统”。



它的主链路应该是：



Schema 变更输入 → 变更识别 → 项目解析 → SQL/代码映射 → 影响传播 → 风险评分 → 报告/PR 审查



1\. 总体分层



建议你整个系统分成 5 层。



第 1 层：输入接入层



负责接收分析对象。



输入来源包括：



Git 仓库

本地上传项目压缩包

Flyway migration 文件

新旧 schema 文件

PR diff



这一层的目标不是分析，而是把输入统一整理好。



第 2 层：解析抽取层



负责“看懂项目”。



这里要完成：



schema diff

Java 源码 AST 解析

Spring 注解识别

SQL 抽取

实体映射抽取

方法调用关系抽取



这里你可以借 Atlas 做 schema diff。Atlas 的 schema diff 本来就是接受 --from 和 --to 两个 schema 状态，计算差异并生成迁移计划，输入可以是数据库 URL、SQL schema、HCL schema 或 migration 目录。

Java 解析可以借 JavaParser，它已经把 Symbol Solver 集成进主项目，支持 AST、符号解析和整仓分析。

注意：这里的引用有点混乱，JavaParser的证据没单独搜到更合适结果。为了稳妥，下面不再对 JavaParser 做事实性细节展开到很细。



第 3 层：统一表示与图构建层



这是你的第一个原创核心。



你要把不同来源的信息统一成一张图：



schema 变更节点

table / column 节点

SQL 节点

Java 方法节点

Repository / Service / Controller 节点

API 节点

Test 节点



边的类型例如：



references\_column

belongs\_to\_table

declared\_in\_method

calls

exposes\_api

covered\_by\_test



这层是你和“普通搜索器”拉开差距的地方。



第 4 层：分析决策层



这是第二个原创核心。



这里放 3 个引擎：



直接命中引擎

找出哪些 SQL / 字段 / XML / 注解直接命中变更点。

影响传播引擎

从直接命中点一路向上游传播到方法、服务、接口、测试。

风险评分引擎

对所有受影响节点排序，告诉用户先处理什么。



这一层是整个作品最像“核心算法”的地方。



第 5 层：产品交互层



这就是评委看到的系统。



包含：



项目总览页

变更分析页

影响图谱页

风险详情页

PR 审查结果页

报告导出页



这一层不需要做得花哨，但必须清晰、稳定、能 10 分钟打透。



二、系统主流程



你要把系统流程固定成下面这条。



流程 A：离线分析流程



适合初版 MVP。



用户接入项目

用户上传旧 schema、新 schema 或 migration

系统运行 schema diff

系统解析 Java 项目与 SQL

系统构建依赖图

系统运行影响传播

系统计算风险分数

系统展示结果并导出报告

流程 B：PR 审查流程



适合第二阶段。



用户选择某个 PR

系统读取 PR 中的 migration / schema 改动

自动执行影响分析

输出高风险变更说明

自动生成 PR 评论文本

三、后端模块划分



后端我建议你按“职责清晰”划分，不要一上来追求微服务。



目录建议：



backend/

&#x20; api/

&#x20; ingest/

&#x20; schemadiff/

&#x20; parser/

&#x20; extractor/

&#x20; graph/

&#x20; analyzer/

&#x20; risk/

&#x20; review/

&#x20; report/

&#x20; common/

1\. ingest



负责项目接入。



功能：



接收 zip 包

克隆 Git 仓库

管理项目版本

读取 PR diff

识别项目类型

2\. schemadiff



负责数据库变更识别。



功能：



调用 Atlas 或同类工具

生成标准化的变更对象

判断是否为 breaking change



输出给其他模块的是统一对象，不是原始 SQL 文本。



3\. parser



负责源码解析。



功能：



Java 文件 AST 解析

注解识别

类/方法/字段索引

Controller / Service / Repository / Entity 识别

4\. extractor



负责抽取数据库访问相关对象。



功能：



JPA Entity -> table/column 映射

@Query 抽取

JDBC SQL 抽取

MyBatis XML 解析

Test 代码识别

5\. graph



负责统一图构建。



功能：



节点建模

边建模

图存储或内存图组织

图查询接口



初版不一定要上图数据库。

你完全可以先用内存图 + 关系表混合实现。



6\. analyzer



负责影响传播。



功能：



直接命中分析

向上游传播

证据链生成

置信度计算

7\. risk



负责风险评分。



功能：



评分因子管理

风险分数计算

风险等级划分

Top-K 排序



CodeQL 的 Java 数据流文档很适合给你这层提供方法论参考：它区分 local data flow、global data flow 和 taint tracking；局部分析更快更精确，全局分析更强但更重。你自己的系统也可以借这个思路，先做“局部直接命中”，再做“全局传播”。



8\. review



负责 PR 审查集成。



功能：



生成 PR 评论

输出风险摘要

决定 warning 或 block

9\. report



负责报告输出。



功能：



HTML/PDF 报告

JSON 导出

历史记录对比

四、核心数据结构设计



这里是最关键的。

你现在必须先把“系统里到底有哪些对象”定下来。



1\. SchemaChange



这是最核心的输入对象。



{

&#x20; "changeId": "chg\_001",

&#x20; "type": "DROP\_COLUMN",

&#x20; "tableName": "orders",

&#x20; "columnName": "status",

&#x20; "oldType": "varchar(16)",

&#x20; "newType": null,

&#x20; "breaking": true,

&#x20; "sourceFile": "V12\_\_drop\_orders\_status.sql"

}

建议支持的类型

ADD\_TABLE

DROP\_TABLE

ADD\_COLUMN

DROP\_COLUMN

ALTER\_COLUMN\_TYPE

RENAME\_COLUMN

ADD\_INDEX

DROP\_INDEX

ALTER\_CONSTRAINT

2\. CodeEntity



表示项目中的代码实体。



{

&#x20; "entityId": "code\_102",

&#x20; "entityType": "METHOD",

&#x20; "name": "queryByStatus",

&#x20; "className": "OrderRepository",

&#x20; "filePath": "src/main/java/.../OrderRepository.java",

&#x20; "startLine": 34,

&#x20; "endLine": 48

}

entityType 建议包括

ENTITY

REPOSITORY

SERVICE

CONTROLLER

METHOD

API

TEST

SQL\_BLOCK

XML\_MAPPER

3\. SqlReference



表示一段 SQL 及其结构化信息。



{

&#x20; "sqlId": "sql\_201",

&#x20; "sqlType": "SELECT",

&#x20; "rawSql": "select \* from orders where status = ?",

&#x20; "tables": \["orders"],

&#x20; "columns": \["orders.status"],

&#x20; "declaredIn": "OrderMapper.xml",

&#x20; "ownerMethodId": "code\_102"

}



这个对象特别重要，因为它是schema 变更和 Java 代码之间的桥。



4\. GraphNode



统一图节点。



{

&#x20; "nodeId": "node\_5001",

&#x20; "nodeType": "COLUMN",

&#x20; "refId": "orders.status",

&#x20; "label": "orders.status"

}

nodeType 建议包括

CHANGE

TABLE

COLUMN

SQL

METHOD

CLASS

SERVICE

CONTROLLER

API

TEST

5\. GraphEdge



统一图边。



{

&#x20; "edgeId": "edge\_9001",

&#x20; "fromNode": "node\_change\_1",

&#x20; "toNode": "node\_sql\_21",

&#x20; "edgeType": "DIRECTLY\_MATCHES",

&#x20; "weight": 0.95,

&#x20; "evidence": "orders.status appears in WHERE clause"

}

edgeType 建议包括

DIRECTLY\_MATCHES

REFERENCES\_COLUMN

REFERENCES\_TABLE

DECLARED\_IN

CALLS

EXPOSES

COVERED\_BY

DERIVED\_FROM

6\. ImpactResult



影响分析输出对象。



{

&#x20; "changeId": "chg\_001",

&#x20; "affectedNodeId": "node\_api\_33",

&#x20; "affectedType": "API",

&#x20; "riskScore": 87.5,

&#x20; "riskLevel": "HIGH",

&#x20; "confidence": 0.88,

&#x20; "evidencePath": \[

&#x20;   "orders.status",

&#x20;   "sql\_201",

&#x20;   "OrderRepository.queryByStatus",

&#x20;   "OrderService.listOrders",

&#x20;   "/api/orders/list"

&#x20; ]

}



这就是你前端风险详情页的主要来源。



7\. RiskFactor



风险评分因子对象。



{

&#x20; "changeSeverity": 0.9,

&#x20; "dependencyStrength": 0.8,

&#x20; "exposureLevel": 0.95,

&#x20; "testGap": 0.7,

&#x20; "propagationDepth": 0.6

}



后面可以组合成最终风险分数。



五、数据库表设计（平台自身数据库）



你的平台本身也需要数据库。

建议最小化设计，先做 8 张表。



1\. projects



存项目基本信息



字段建议：



id

name

repo\_url

tech\_stack

default\_branch

created\_at

2\. project\_versions



存某次分析对应的项目版本



字段建议：



id

project\_id

commit\_sha

branch\_name

source\_type

imported\_at

3\. schema\_changes



存标准化后的变更对象



字段建议：



id

version\_id

change\_type

table\_name

column\_name

old\_type

new\_type

is\_breaking

source\_file

4\. code\_entities



存代码实体索引



字段建议：



id

version\_id

entity\_type

class\_name

method\_name

file\_path

start\_line

end\_line

5\. sql\_references



存抽取到的 SQL



字段建议：



id

version\_id

raw\_sql

sql\_type

tables\_json

columns\_json

owner\_entity\_id

6\. graph\_edges



存统一图边



字段建议：



id

version\_id

from\_node

to\_node

edge\_type

weight

evidence

7\. analysis\_runs



存一次分析任务



字段建议：



id

project\_id

version\_id

trigger\_type

status

started\_at

finished\_at

8\. impact\_results



存最终分析结果



字段建议：



id

run\_id

change\_id

affected\_entity\_id

affected\_type

risk\_score

risk\_level

confidence

evidence\_path\_json

六、核心算法先怎么做



你现在不要想着一步到位做复杂图神经网络。

国奖更看重你是否做得完整、可解释、可验证。



算法 1：直接命中分析



目标：



给一个 schema change

找直接受影响的 SQL / Entity / Mapper



规则先做简单明确：



DROP\_COLUMN：命中所有直接引用该列的位置

ALTER\_COLUMN\_TYPE：命中相关字段和参数绑定位置

DROP\_TABLE：命中所有涉及该表的 SQL 和实体

RENAME\_COLUMN：命中旧列引用，并输出 rename 风险



这一层先做准。



算法 2：影响传播



建议先做带权 BFS。



传播规则例如：



COLUMN → SQL：权重 1.0

SQL → METHOD：权重 0.9

METHOD → SERVICE：权重 0.75

SERVICE → CONTROLLER：权重 0.7

CONTROLLER → API：权重 0.9

API → TEST：权重 0.6



每次传播累计一个置信度衰减。

这样你后面可以解释为什么某个结果是高置信度。



算法 3：风险评分



先用线性模型，不要一开始上机器学习。



例如：



RiskScore = 0.30\*ChangeSeverity + 0.25\*DependencyStrength + 0.20\*ExposureLevel + 0.15\*TestGap + 0.10\*PropagationDepth



因子建议

ChangeSeverity：删列/改类型权重大

DependencyStrength：显式 SQL > 模糊推断

ExposureLevel：API 暴露更高

TestGap：无测试覆盖更高

PropagationDepth：链越长越危险

七、前端页面设计



前端别做太重。

4 个页面足够。



1\. 项目总览页



展示：



项目名称

技术栈

最近分析次数

最近高风险变更数

2\. 变更分析页



展示：



本次 schema 变更列表

是否为 breaking change

高风险对象数量

分析状态

3\. 影响图谱页



展示：



左边：变更项

中间：SQL / 方法 / 服务

右边：API / 测试



支持点击展开证据链。



4\. 风险详情页



展示：



风险分数

风险等级

命中的代码位置

证据路径

建议修复动作

八、开发里程碑

阶段 1：最小可行版本 MVP（第 1–3 周）



目标：打通主链路的前半段



要完成：



项目接入

Atlas 跑通 schema diff

Java 项目基础解析

识别 Controller / Service / Repository / Entity

支持 JPA 与 @Query 抽取

输出直接命中结果

阶段验收



你应该能做到：



给一组 schema 变化，系统输出哪些 SQL 和代码位置直接受影响。



阶段 2：核心分析版本（第 4–6 周）



目标：做出影响链



要完成：



统一图构建

影响传播

风险评分

风险详情接口

MyBatis XML 支持

JDBC SQL 支持

阶段验收



你应该能做到：



删除一个字段后，系统能一路指出受影响的方法、服务和接口，并按风险排序。



阶段 3：产品化版本（第 7–9 周）



目标：做成完整作品



要完成：



Web 可视化页面

历史分析记录

PR 审查输出

HTML/PDF 报告

实验数据采集

阶段验收



你应该能做到：



用一个真实 Spring Boot 项目现场演示完整分析流程。



阶段 4：竞赛版本（第 10–12 周）



目标：冲奖打磨



要完成：



对比实验

指标图表

录屏 demo

申报书

PPT

答辩问答准备

九、你现在立刻要做的 7 件事



今天就能开工的版本：



新建仓库 schemascope

建立 backend / frontend / docs / benchmark

把 README 放进去

写 docs/architecture.md

定义 SchemaChange / CodeEntity / ImpactResult 三个核心对象

选 2 个 Spring Boot benchmark 项目

跑通一次 schema diff demo

十、这版蓝图的核心判断



如果你按这版做，项目会有 3 个明显优势：



1\. 题目真



数据库变更影响代码，这是研发里真实高频痛点。



2\. 技术硬



不是普通管理系统，而是：



schema diff

程序分析

统一图建模

风险排序

PR 审查闭环

3\. 容易演示



你只要现场删一个字段，系统立刻指出受影响 API 和测试，就很有冲击力。

