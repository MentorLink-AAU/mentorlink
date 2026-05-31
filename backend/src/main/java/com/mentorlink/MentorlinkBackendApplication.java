package com.mentorlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** MentorLink backend entry point; enables async and scheduling. */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MentorlinkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentorlinkBackendApplication.class, args);
    }
}
