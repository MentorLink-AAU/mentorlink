package com.mentorlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RecommenderExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService recommenderExecutor() {
        return Executors.newFixedThreadPool(2);
    }
}
