package com.schemascope.service;
/*
它定义了一个能力：
给我一个 AnalysisRequest，我返回一组 ImpactResult
 */
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;

import java.util.List;

public interface AnalysisService {

    List<ImpactResult> analyze(AnalysisRequest request);
}