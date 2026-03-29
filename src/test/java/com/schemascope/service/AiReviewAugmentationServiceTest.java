package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.SchemaChange;
import com.schemascope.domain.AiReviewAugmentation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiReviewAugmentationServiceTest {

    @Test
    void shouldGenerateReviewSummaryRisksAndChecklist() {
        SchemaChange change = new SchemaChange(
                "chg-drop-column-owners-last-name",
                ChangeType.DROP_COLUMN,
                "owners",
                "last_name",
                "VARCHAR(80)",
                null,
                true,
                "schema-diff"
        );

        List<ImpactResult> impactResults = List.of(
                new ImpactResult(
                        "chg-drop-column-owners-last-name",
                        "OwnerRepository",
                        "REPOSITORY",
                        95.0,
                        RiskLevel.HIGH,
                        0.95,
                        List.of(
                                "Schema change: DROP_COLUMN owners.last_name",
                                "Matched SQL: OwnerRepository#findByLastName#NATIVE_QUERY"
                        ),
                        ImpactRelationLevel.DIRECT
                ),
                new ImpactResult(
                        "chg-drop-column-owners-last-name",
                        "OwnerController",
                        "REST_CONTROLLER",
                        74.0,
                        RiskLevel.MEDIUM,
                        0.74,
                        List.of(
                                "Schema change: DROP_COLUMN owners.last_name",
                                "Propagation: OwnerController references OwnerService"
                        ),
                        ImpactRelationLevel.INDIRECT
                )
        );

        AiReviewAugmentationService service = new AiReviewAugmentationService();
        AiReviewAugmentation augmentation = service.augment(change, impactResults);

        System.out.println("AiReviewAugmentation = " + augmentation);

        assertTrue(augmentation.getSummary().contains("OwnerRepository"));
        assertFalse(augmentation.getKeyRisks().isEmpty());
        assertFalse(augmentation.getSuggestedActions().isEmpty());
        assertFalse(augmentation.getReleaseChecklist().isEmpty());

        assertTrue(augmentation.getKeyRisks().stream()
                .anyMatch(item -> item.contains("Repository-layer")));

        assertTrue(augmentation.getReleaseChecklist().stream()
                .anyMatch(item -> item.contains("manual reviewer sign-off")));
    }
}