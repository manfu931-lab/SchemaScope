# SchemaScope 页面布局规范（答辩版）

## 1. Review Page（单次变更审查页）

### 页面目标
用于展示一次 schema change 的影响分析结果，适合 reviewer 和评委快速理解：
- 改了什么
- 影响了哪些模块
- 为什么判断它们受影响
- 风险在哪里
- 上线前该做什么

### 数据源
- review-page.json

### 首屏区域
1. 标题区
   - 标题：SchemaScope PR Review - {projectName}
   - 副标题：{summary}
   - Verdict
   - Risk Level

2. 核心指标区
   - Total Impacted
   - Direct Impact
   - Indirect Impact
   - Risk Level
   - Verdict

3. 风险与行动区（左右双栏）
   - Key Risks
   - Suggested Actions
   - Release Checklist

### 主体区域
4. Top Impact Cards
   - affectedObject
   - affectedType
   - relationLevel
   - riskLevel
   - confidenceScore
   - evidencePath（支持展开）

5. Markdown Review Panel
   - 展示 markdownComment
   - 用于答辩时朗读 / reviewer 快速浏览

### 页面原则
- 首屏必须在 5 秒内看懂
- 风险、结论、行动优先
- 证据链必须可展开，但默认不要铺满整屏


## 2. Showcase Dashboard（系统展示页）

### 页面目标
用于答辩演示，强调“系统能力”而不是单个 case 细节。

### 数据源
- showcase-dashboard.json

### 首屏区域
1. Hero 区
   - Title
   - Executive Summary
   - Verdict
   - Risk Level

2. Metric Cards
   - 4~6 个卡片
   - 适合横向排布

3. Core Highlights
   - 3~5 条
   - 强调：
     - evidence-driven
     - multi-layer propagation
     - AI augmentation
     - benchmark-backed

### 主体区域
4. Demo Steps
   - 演示步骤列表
   - 答辩时按顺序点击/讲解

5. Defense Talking Points
   - 给演讲者的提示词
   - 不直接面向用户

6. Embedded Review Page Summary
   - 只展示 review page 的精简版
   - 不要全量塞入，避免太长

7. Markdown Brief
   - 一段适合答辩展示的 markdown 简报

### 页面原则
- 展示系统全貌
- 不陷入单个字段细节
- 强调“输入 schema change -> 输出 evidence-backed review”这条链


## 3. Benchmark Dashboard（实验评测页）

### 页面目标
用于展示系统实验结果，体现项目不是 demo，而是有 benchmark 支撑的工程/研究系统。

### 数据源
- petclinic-external-dashboard.json

### 首屏区域
1. 标题区
   - Benchmark title
   - summary

2. Metric Cards
   - Average Precision
   - Average Recall
   - Direct Hit@3
   - Evidence Coverage
   - Relation Accuracy

3. Highlights
   - 自动生成的亮点
   - 比如：
     - Recall strong
     - Evidence coverage strong
     - Precision remains main target

### 主体区域
4. Case Table / Case Cards
   - caseId
   - predictedAffectedObjects
   - precision
   - recall
   - evidenceCoverage
   - relationAccuracy

5. Benchmark Notes
   - 解释当前 benchmark 的意义
   - 说明 precision / recall 分别代表什么

### 页面原则
- 先给指标，再给 case
- 首屏必须能体现“我们有真实项目 benchmark”
- 重点突出 PetClinic 外部 benchmark


## 4. 首页导航建议

首页应只放 3 个入口：
- Review
- Showcase
- Benchmark

不要在首页堆太多图和字段。
首页的任务只是告诉评委：
“SchemaScope 是一个能分析 schema change、给出证据链、支持 AI 审查增强、并有 benchmark 支撑的系统。”


## 5. UI 风格建议

- 风格：专业、简洁、偏工程系统，不要做花哨炫技风
- 主色：深蓝 / 蓝绿 / 中性灰
- 风险色：
  - HIGH -> red
  - MEDIUM -> amber
  - LOW -> green
- 卡片：圆角、弱阴影、信息分层清楚
- 图谱：支持 hover / click 展开 evidence path
- 不要让页面第一屏出现过长表格


## 6. 答辩演示顺序建议

1. 先打开 Showcase Dashboard
2. 讲系统输入输出链路
3. 打开 Review Page
4. 展示 evidence path 和 AI review augmentation
5. 打开 Benchmark Dashboard
6. 展示 PetClinic 外部 benchmark 指标
7. 收尾时强调：
   - explainable
   - benchmark-backed
   - engineering-ready