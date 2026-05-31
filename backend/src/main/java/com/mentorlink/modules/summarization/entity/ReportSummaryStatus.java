package com.mentorlink.modules.summarization.entity;

/** Status of NLP report summarization: PENDING → PROCESSING → DONE or FAILED. */
public enum ReportSummaryStatus {
    PENDING,
    PROCESSING,
    DONE,
    FAILED
}
