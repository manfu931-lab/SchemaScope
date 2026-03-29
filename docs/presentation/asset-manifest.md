# SchemaScope 展示资产清单

## 1. 后端展示 JSON

### Review
- target/presentation-assets/review-page.json

### Showcase
- target/presentation-assets/showcase-dashboard.json

### Benchmark
- target/benchmark-dashboard/petclinic-external-dashboard.json

---

## 2. 页面与数据对应关系

### Review Page
数据源：
- review-page.json

核心字段：
- title
- summary
- verdict
- riskLevel
- keyRisks
- suggestedActions
- releaseChecklist
- topImpactCards
- metricCards
- markdownComment

### Showcase Dashboard
数据源：
- showcase-dashboard.json

核心字段：
- title
- executiveSummary
- verdict
- riskLevel
- metricCards
- coreHighlights
- demoSteps
- defenseTalkingPoints
- reviewPage
- markdownBrief

### Benchmark Dashboard
数据源：
- petclinic-external-dashboard.json

核心字段：
- title
- summary
- metricCards
- caseViews
- highlights

---

## 3. 演示时优先展示的内容

### 首先展示
- Showcase Dashboard

### 然后展示
- Review Page
- evidence path
- AI augmentation

### 最后展示
- Benchmark Dashboard
- 外部 PetClinic benchmark 指标

---

## 4. 资产使用原则

- 展示层优先读取 JSON 工件，不直接耦合底层领域对象
- 每类页面只消费对应 JSON
- 页面不要同时直接拼接多个原始分析对象
- 所有页面都要保留“首屏 5 秒可理解”的原则