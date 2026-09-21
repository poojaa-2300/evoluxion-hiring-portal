package com.evoluxion.hiring_portal.config;

import com.evoluxion.hiring_portal.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF because this is a stateless REST API
            .csrf(csrf -> csrf.disable())

            // JWT-based authentication
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            .authorizeHttpRequests(auth -> auth

                // ==========================================
                // PUBLIC AUTHENTICATION ENDPOINTS
                // ==========================================

                .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/verify-otp",
                        "/api/auth/resend-otp",
                        "/api/auth/forgot-password",
                        "/api/auth/verify-reset-otp",
                        "/api/auth/reset-password"
                ).permitAll()


                // ==========================================
                // SWAGGER / API DOCUMENTATION
                // ==========================================

                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()


                // ==========================================
                // CANDIDATE APIs
                // ==========================================

                .requestMatchers("/api/candidate/**")
                .hasRole("CANDIDATE")


                // ==========================================
                // INTERVIEWER APIs
                // ==========================================

                .requestMatchers("/api/interviewer/**")
                .hasRole("INTERVIEWER")


                // ==========================================
                // HR APIs
                // ==========================================

                .requestMatchers("/api/hr/**")
                .hasRole("HR")


                // ==========================================
                // ADMIN APIs
                // ==========================================

                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")


                // ==========================================
                // EVERYTHING ELSE
                // ==========================================

                .anyRequest().authenticated()
            )

            // JWT filter runs before Spring's username/password filter
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    org.springframework.security.web.authentication
                            .UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}