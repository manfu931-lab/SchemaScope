package com.schemascope.service;

import com.schemascope.domain.AiReviewAugmentation;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiReviewAugmentationService {

    public AiReviewAugmentation augment(SchemaChange change, List<ImpactResult> impactResults) {
        AiReviewAugmentation augmentation = new AiReviewAugmentation();

        augmentation.setSummary(buildSummary(change, impactResults));
        augmentation.setKeyRisks(buildKeyRisks(change, impactResults));
        augmentation.setSuggestedActions(buildSuggestedActions(change, impactResults));
        augmentation.setReleaseChecklist(buildReleaseChecklist(change, impactResults));

        return augmentation;
    }

    private String buildSummary(SchemaChange change, List<ImpactResult> impactResults) {
        if (impactResults == null || impactResults.isEmpty()) {
            return "No strong downstream impact evidence was found for this schema change. Manual verification is still recommended.";
        }

        long directCount = impactResults.stream()
                .filter(result -> result.getRelationLevel() != null
                        && result.getRelationLevel().name().equals("DIRECT"))
                .count();

        List<String> topObjects = impactResults.stream()
                .limit(3)
                .map(ImpactResult::getAffectedObject)
                .collect(Collectors.toList());

        return "This schema change is likely to affect "
                + impactResults.size()
                + " components, including "
                + String.join(", ", topObjects)
                + ". Direct evidence exists for "
                + directCount
                + " component(s), so this change should be reviewed before release.";
    }

    private List<String> buildKeyRisks(SchemaChange change, List<ImpactResult> impactResults) {
        List<String> risks = new ArrayList<>();

        if (impactResults == null || impactResults.isEmpty()) {
            risks.add("Potential hidden impact cannot be ruled out because no strong evidence chain was produced.");
            return risks;
        }

        boolean hasHighRisk = impactResults.stream().anyMatch(result -> result.getRiskLevel() == RiskLevel.HIGH);
        boolean touchesController = impactResults.stream().anyMatch(result ->
                "CONTROLLER".equals(result.getAffectedType()) || "REST_CONTROLLER".equals(result.getAffectedType()));
        boolean touchesRepository = impactResults.stream().anyMatch(result ->
                "REPOSITORY".equals(result.getAffectedType()));

        if (hasHighRisk) {
            risks.add("At least one impacted component is scored as HIGH risk, which means runtime breakage is plausible if the schema and application code are not updated together.");
        }

        if (touchesRepository) {
            risks.add("Repository-layer query logic may no longer align with the updated schema, especially around field names, derived queries, native SQL, or mapper definitions.");
        }

        if (touchesController) {
            risks.add("Controller/API behavior may change indirectly because request flows depend on impacted repository or service methods.");
        }

        if (change != null && change.getColumnName() != null && !change.getColumnName().isBlank()) {
            risks.add("Column-level changes often break filtering, sorting, validation, DTO mapping, and query predicates that still reference '" + change.getColumnName() + "'.");
        }

        return risks;
    }

    private List<String> buildSuggestedActions(SchemaChange change, List<ImpactResult> impactResults) {
        List<String> actions = new ArrayList<>();

        actions.add("Review the top impacted repository and service methods first, because they are the closest executable evidence to the schema change.");
        actions.add("Search for all query paths that reference the changed table or column, including native SQL, derived query methods, JDBC code, and XML mappers.");
        actions.add("Add or update regression tests that cover the affected request flow end-to-end.");

        boolean hasController = impactResults != null && impactResults.stream().anyMatch(result ->
                "CONTROLLER".equals(result.getAffectedType()) || "REST_CONTROLLER".equals(result.getAffectedType()));
        if (hasController) {
            actions.add("Retest controller endpoints that depend on the impacted flow, including request parameter binding and returned view/API payloads.");
        }

        if (change != null && change.getTableName() != null && !change.getTableName().isBlank()) {
            actions.add("Validate that the deployed schema version and application version are rolled out in the correct order for table '" + change.getTableName() + "'.");
        }

        return actions;
    }

    private List<String> buildReleaseChecklist(SchemaChange change, List<ImpactResult> impactResults) {
        List<String> checklist = new ArrayList<>();

        checklist.add("Confirm the schema migration has been applied successfully in the target environment.");
        checklist.add("Confirm impacted repository queries have been updated and reviewed.");
        checklist.add("Run automated tests covering the impacted service/controller path.");
        checklist.add("Verify no stale SQL, mapper XML, or derived query method still references removed or renamed fields.");
        checklist.add("Check production rollout order, rollback plan, and monitoring signals before release.");

        if (change != null && change.getColumnName() != null && !change.getColumnName().isBlank()) {
            checklist.add("Verify reads, writes, filters, and serializers no longer depend on column '" + change.getColumnName() + "'.");
        }

        if (impactResults != null && impactResults.stream().anyMatch(result -> result.getRiskLevel() == RiskLevel.HIGH)) {
            checklist.add("Require manual reviewer sign-off because HIGH-risk impact was detected.");
        }

        return checklist;
    }
}