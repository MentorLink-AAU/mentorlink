package com.mentorlink.common.auditing;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables JPA auditing (e.g. createdAt, updatedAt). */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
}