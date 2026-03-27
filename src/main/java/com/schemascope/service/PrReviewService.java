package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.PrReviewReport;

public interface PrReviewService {

    PrReviewReport review(AnalysisRequest request);
}