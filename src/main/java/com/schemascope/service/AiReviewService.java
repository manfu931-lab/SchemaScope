package com.schemascope.service;

import com.schemascope.domain.AiReviewResult;
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.PrReviewReport;

public interface AiReviewService {

    AiReviewResult review(AnalysisRequest request, PrReviewReport report);
}