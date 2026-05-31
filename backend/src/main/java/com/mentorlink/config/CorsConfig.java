package com.mentorlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** CORS: localhost for dev + APP_FRONTEND_URL for production (comma-separated). */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins());
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static List<String> allowedOrigins() {
        List<String> origins = new ArrayList<>(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://mellow-reprieve-production-21f1.up.railway.app"
        ));
        String env = System.getenv("APP_FRONTEND_URL");
        if (env != null && !env.isBlank()) {
            for (String part : env.split(",")) {
                String origin = part.trim();
                if (origin.startsWith("\"") && origin.endsWith("\"")) {
                    origin = origin.substring(1, origin.length() - 1);
                }
                if (!origin.isEmpty() && !origins.contains(origin)) {
                    origins.add(origin);
                }
            }
        }
        return origins;
    }
}
