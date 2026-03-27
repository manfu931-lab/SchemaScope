SimpleImpactAnalyzerTest.java 测试“给定一条 schema 变化，是否能生成正确结果”
SchemaChangeFactoryTest.java 测试请求对象能否正确转换成 SchemaChange
RealSchemaAnalysisServiceTest.java 测试“从 schema 文件出发”能不能跑完整分析链
SchemaChangeComponentMapperTest.java 先用手工小样本测试映射规则
ExternalProjectMappingTest.java 拿 spring-petclinic 做第一次真实映射测试
ComponentImpactResultBuilderTest.java 测试候选组件能否正确转成 ImpactResult
ExternalMappedAnalysisServiceTest.java 用真实外部项目跑一次完整映射分析服务
ImpactResultRankerTest.java 排序是否正确 是否真的只保留前 5 个结果
ExternalSchemaToProjectAnalysisTest.java 
        跑完整闭环：
        读 petclinic schema v1/v2
        自动 diff
        扫描 spring-petclinic
        自动映射
        返回真实影响结果
ImpactCandidateGrouperTest.java 验证 ImpactCandidateGrouper 真的能把结果分成两组。
ImpactResultGrouperTest.java 测试分组器能不能按 relationLevel 正确分组
GroupedExternalAnalysisTest.java 用真实外部项目分析结果，验证分组结果是否符合预期
SchemaChangeSqlMatcherTest.java 这一步的目标是把 SchemaChange -> SQL 命中层 独立跑通。
SqlImpactPropagatorTest.java 
MockAnalysisServiceEvidenceChainTest.java 
MockPrReviewServiceTest.java 先从 service 层把“分析结果 -> review report”打通。
