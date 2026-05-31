package com.mentorlink.config;

import com.mentorlink.security.jwt.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Spring Security: JWT auth, role-based access, stateless sessions. */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (since using JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // 🔐 Authenticated auth endpoints (JWT required)
                        .requestMatchers(
                                "/api/auth/me",
                                "/api/auth/update",
                                "/api/auth/change-password"
                        ).authenticated()

                        // 🔓 Public auth endpoints (login, register, password reset)
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/login/**",
                                "/api/auth/register/**",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()

                        .requestMatchers("/actuator/**").permitAll()

                        // 👤 User endpoints (list/manage - admin only)
                        .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")

                        // 📋 Profile & Dashboard
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/dashboard/student").hasRole("STUDENT")
                        .requestMatchers("/api/dashboard/faculty").hasRole("FACULTY")
                        .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")

                        // 📁 Serve uploaded files (profile photos, etc.)
                        .requestMatchers("/api/files/**").authenticated()

                        // 👨‍🏫 Faculty endpoints (list is for students too)
                        .requestMatchers("/api/faculty/list").authenticated()
                        .requestMatchers("/api/faculty/**").hasRole("FACULTY")

                        // 👑 Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 📁 Projects & Groups
                        .requestMatchers("/api/projects/**").authenticated()
                        .requestMatchers("/api/groups/**").authenticated()

                        // 📅 Deadlines (read for all)
                        .requestMatchers("/api/deadlines").authenticated()

                        // 📄 Submissions
                        .requestMatchers("/api/submissions/**").authenticated()

                        // 🔍 Faculty Recommender (TF-IDF + Cosine Similarity)
                        .requestMatchers("/api/recommend/**").authenticated()
                        .requestMatchers("/api/run/**").hasRole("ADMIN")
                        .requestMatchers("/api/recommender/**").hasRole("ADMIN")

                        // 📢 Notifications & Chat
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/ws/**").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Stateless session (VERY IMPORTANT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // DAO authentication provider (for normal login)
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // DAO Authentication Provider (Email + Password login)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
