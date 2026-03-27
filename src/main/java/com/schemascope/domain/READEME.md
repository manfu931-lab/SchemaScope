ChangeType.java 定义“变更类型”有哪些
SchemaChange.java 定义“一次 schema 变化”长什么样
AnalysisRequest.java 表示一次分析任务的输入参数
RiskLevel.java 定义风险等级
ImpactResult.java 表示一条分析结果
SchemaColumn.java 表示一个列
SchemaTable.java 表示一个表
ParsedSchema.java 表示“一个 schema 文件解析后的结果”
JavaComponentType.java 定义 Java 组件类型枚举
JavaComponent.java 表示一个被扫描出来的 Java 组件
JavaProjectScanResult.java 表示一次项目扫描的总结果
ComponentImpactCandidate.java 表示一个“候选受影响组件”
ImpactRelationLevel.java 定义关系层级：直接 / 间接
GroupedImpactCandidates.java 表示分层后的结果：directCandidates / indirectCandidates
GroupedImpactResults.java 表示最终结果分组：直接结果 / 间接结果
SqlSourceType.java 告诉系统“这条 SQL 是从哪里抽出来的”
SqlImpactCandidate.java 这个类是“SchemaChange 命中了哪条 SQL 证据”的结果对象
ReviewVerdict.java 这是 PR 审查的最终结论，不再只是给一堆 ImpactResult，而是明确告诉评委/开发者：可以直接过，需要重点 review，应该阻断合并
ReviewActionItem.java 让系统不仅告诉你“哪里受影响”，还给出“下一步该做什么”。
PrReviewReport.java 这个对象就是“PR 审查输出层”的核心。它把前面分析出来的证据链，包装成：
        verdict
        overall risk
        top risks
        direct / indirect 分组
        review checklist
        action items
        可直接发到 PR 评论区的 markdown comment
ApiEndpointImpact.java 而是能进一步说：
        GET /owners 受影响
        它来自 OwnerController
        风险等级是什么
        是直接还是间接传播来的
TestImpactHint.java 这个类不是“测试已经失败”，而是“哪些测试最值得优先看 / 补 / 回归”
ImpactSurfaceSummary.java 把“接口面”和“测试面”收成一个对象，后面直接塞进 PR review report。
SelectedTestCase.java 这个类表示“已经排好优先级的测试任务”。和第 7 步的 TestImpactHint 不同，它不是“可能相关”，而是“建议你先跑/先补的测试项”。
TestExecutionPlan.java 把测试建议收成真正的“执行计划”
EvidenceGraphNode.java 这是图里的节点对象。以后图里每个点都能落成结构化节点
EvidenceGraphEdge.java 这是图里的边。
        它能明确表达：
        schema change 命中了哪个 SQL owner
        哪个组件传播到哪个组件
        哪个 controller 对应哪个 endpoint
        哪个组件映射到哪个测试计划
EvidenceGraphExport.java 这个类就是“图导出结果”。
        后面一个接口直接把它返回给前端，就能做：
        JSON graph 渲染
        Mermaid 预览
        markdown 附图
DefenseMetricCard.java 这是展示包里的“指标卡片”
DefenseShowcasePack.java 这个对象就是“答辩展示包”。
        它不是底层分析对象，而是一个：
        可以直接喂给前端
        可以直接拿去写 PPT
        可以直接做演示脚本
