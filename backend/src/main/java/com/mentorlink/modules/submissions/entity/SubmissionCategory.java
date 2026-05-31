package com.mentorlink.modules.submissions.entity;

/**
 * Fixed submission types per group. Each group must submit exactly 3: Report, Research Paper, PPT.
 */
public enum SubmissionCategory {
    REPORT,
    RESEARCH_PAPER,
    PPT;

    public static SubmissionCategory fromString(String value) {
        if (value == null || value.isBlank()) return null;
        String norm = value.toUpperCase().replace(" ", "_").replace("-", "_");
        if ("RESEARCHPAPER".equals(norm) || "RESEARCH_PAPER".equals(norm)) return RESEARCH_PAPER;
        if ("REPORT".equals(norm)) return REPORT;
        if ("PPT".equals(norm) || "POWERPOINT".equals(norm) || "PRESENTATION".equals(norm)) return PPT;
        try {
            return valueOf(norm);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
