# SchemaScope

SchemaScope 是一个面向 Spring Boot / Java 项目的 **schema 变更影响分析与研发审查辅助系统**。  
它的核心目标不是只做 schema diff，而是把数据库变更的影响链路一直追踪到：

- SQL 访问点
- Repository / DAO
- Service
- Controller / API
- 测试建议与测试执行计划
- PR Review 报告
- 可视化证据图

---

## 1. 项目定位

传统 schema 变更评估往往依赖：

- 人工经验排查
- grep 搜索
- 类名 / 表名猜测
- 事后接口回归

这种方式存在两个问题：

1. **解释性弱**：只能说“可能有影响”，很难说明为什么  
2. **闭环不足**：即使知道组件受影响，也很难继续落到 API、测试和 review 决策

SchemaScope 的定位是：  
**把 schema change 转换成 evidence-driven 的研发影响分析与审查输出。**

---

## 2. 当前能力

当前版本已经支持以下能力：

### 2.1 Schema 变更分析
- 解析旧 schema / 新 schema
- 生成 schema diff
- 支持手工输入 change request

### 2.2 SQL 证据提取
- 提取 `@Query(...)`
- 提取 `jdbcTemplate.query / update / queryForObject`
- 抽取表名、列 token、SQL owner

### 2.3 Evidence-driven 命中与传播
- `SchemaChange -> SQL Access Point`
- `SQL Access Point -> Repository / DAO`
- `Repository / DAO -> Service -> Controller`
- 区分 `DIRECT / INDIRECT`

### 2.4 结构化结果输出
- risk score
- risk level
- confidence
- evidence path
- relation level

### 2.5 PR Review 输出
- verdict（APPROVE / REVIEW_REQUIRED / BLOCK）
- action items
- review checklist
- markdown comment

### 2.6 接口与测试影响补全
- 识别受影响 endpoint
- 生成 suggested tests
- 生成 prioritized test execution plan

### 2.7 可视化证据图
- 导出 graph nodes / edges
- 导出 Mermaid
- 支持单独 graph API

### 2.8 答辩展示包
- metric cards
- core highlights
- demo steps
- defense talking points
- review report + graph 一体化输出

---

## 3. 系统主链路

核心分析链如下：

```text
Schema Change
  -> SQL Access Extraction
  -> SQL Match
  -> Component Propagation
  -> API / Test Surface Expansion
  -> PR Review
  -> Evidence Graph
  -> Defense Showcase