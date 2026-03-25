AnalysisService.java 定义“分析服务”应该提供什么能力
impl 先写一个假的分析实现，返回模拟数据
SimpleImpactAnalyzer.java 根据一条 SchemaChange，生成一组影响结果
SchemaChangeFactory.java 专门负责把 AnalysisRequest 转成 SchemaChange
