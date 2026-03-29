package com.schemascope.repository;

import com.schemascope.domain.job.AnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, String> {
}