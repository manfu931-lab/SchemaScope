package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;

public interface DefenseShowcaseService {

    DefenseShowcasePack buildShowcase(AnalysisRequest request);
}