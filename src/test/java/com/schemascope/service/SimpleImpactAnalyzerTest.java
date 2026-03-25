package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.SchemaChange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleImpactAnalyzerTest {

    @Test
    void shouldGenerateImpactResultsForAlterColumnType() {
        SimpleImpactAnalyzer analyzer = new SimpleImpactAnalyzer();
    
        SchemaChange change = new SchemaChange(
                "chg-002",
                ChangeType.ALTER_COLUMN_TYPE,
                "users",
                "email",
                "varchar(64)",
                "varchar(128)",
                true,
                "V13__alter_users_email_type.sql"
        );
    
        List<ImpactResult> results = analyzer.analyze(change);
    
        assertEquals(2, results.size());
        assertEquals("chg-002", results.get(0).getChangeId());
        assertEquals("UserRepository.updateEmail", results.get(0).getAffectedObject());
        assertEquals("METHOD", results.get(0).getAffectedType());
        assertEquals("users.email", results.get(0).getEvidencePath().get(0));
    }
}