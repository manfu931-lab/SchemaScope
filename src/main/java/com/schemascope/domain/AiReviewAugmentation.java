package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class AiReviewAugmentation {

    private String summary;
    private List<String> keyRisks = new ArrayList<>();
    private List<String> suggestedActions = new ArrayList<>();
    private List<String> releaseChecklist = new ArrayList<>();

    public AiReviewAugmentation() {
    }

    public AiReviewAugmentation(String summary,
                                List<String> keyRisks,
                                List<String> suggestedActions,
                                List<String> releaseChecklist) {
        this.summary = summary;
        this.keyRisks = keyRisks == null ? new ArrayList<>() : new ArrayList<>(keyRisks);
        this.suggestedActions = suggestedActions == null ? new ArrayList<>() : new ArrayList<>(suggestedActions);
        this.releaseChecklist = releaseChecklist == null ? new ArrayList<>() : new ArrayList<>(releaseChecklist);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeyRisks() {
        return keyRisks;
    }

    public void setKeyRisks(List<String> keyRisks) {
        this.keyRisks = keyRisks == null ? new ArrayList<>() : new ArrayList<>(keyRisks);
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions == null ? new ArrayList<>() : new ArrayList<>(suggestedActions);
    }

    public List<String> getReleaseChecklist() {
        return releaseChecklist;
    }

    public void setReleaseChecklist(List<String> releaseChecklist) {
        this.releaseChecklist = releaseChecklist == null ? new ArrayList<>() : new ArrayList<>(releaseChecklist);
    }

    @Override
    public String toString() {
        return "AiReviewAugmentation{" +
                "summary='" + summary + '\'' +
                ", keyRisks=" + keyRisks +
                ", suggestedActions=" + suggestedActions +
                ", releaseChecklist=" + releaseChecklist +
                '}';
    }
}