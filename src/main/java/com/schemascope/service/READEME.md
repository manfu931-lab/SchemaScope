AnalysisService.java 定义“分析服务”应该提供什么能力
impl 先写一个假的分析实现，返回模拟数据
SimpleImpactAnalyzer.java 根据一条 SchemaChange，生成一组影响结果
SchemaChangeFactory.java 专门负责把 AnalysisRequest 转成 SchemaChange
SchemaChangeComponentMapper.java 根据 SchemaChange 和 JavaProjectScanResult，找出候选组件
ComponentImpactResultBuilder.java 把候选组件 ComponentImpactCandidate 转成接口最终返回的 ImpactResult
ImpactResultRanker.java 对 ImpactResult 列表进行排序和截断
ImpactCandidateGrouper.java 把候选组件列表分成“直接相关”和“间接相关”
ImpactResultGrouper.java 把 List<ImpactResult> 分组为 GroupedImpactResults
SchemaChangeSqlMatcher.java 这一步做完后，系统第一次具备了这种能力：
    DROP_COLUMN owners.last_name
    去命中 OwnerRepository.findByLastName
    去命中 OwnerJdbcDao.updateOwnerLastName
SqlImpactPropagator.java 让 SpringProjectScanner 能把它识别成 REPOSITORY，后面传播时才能把 SQL owner 映射到组件。
PrReviewService.java 把“分析服务”和“PR review 服务”拆开。
PrReviewReportBuilder.java 这是这一步最核心的 builder。它把前面的分析结果转成真正的 review/report：
    总结本次 schema change
    计算 verdict
    生成 action items
    生成 checklist
    生成可直接贴到 PR 上的 markdown comment
ImpactSurfaceBuilder.java 它做两件事：
    从 受影响 Controller 源码 里解析出真正的接口路径
    从 项目测试源码 里给出最值得优先回归 / 补充的测试建议
TestImpactPlanner.java 它把第 7 步“接口 / 测试提示”升级成真正的排序逻辑
EvidenceGraphExporter.java 这是这一步最核心的类。
    它会把现在已经有的：
    changeSummary
    ImpactResult.evidencePath
    impactedEndpoints
    testExecutionPlan
    统一编造成一张图。
DefenseShowcaseService.java 把“答辩展示包”单独做成一个服务接口，不和分析主链混在一起。
DefenseShowcaseBuilder.java 这是这一步最核心的 builder。它把 review report + graph + test plan 收束成一个真正适合演示的结果。
MockDefenseShowcaseService.java 这里不重复实现分析，只复用你已经做好的 PrReviewService
