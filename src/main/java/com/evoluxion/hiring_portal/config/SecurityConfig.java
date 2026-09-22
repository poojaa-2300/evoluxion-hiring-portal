package com.evoluxion.hiring_portal.config;

import com.evoluxion.hiring_portal.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            // Disable CSRF because this is a REST API
            .csrf(csrf -> csrf.disable())

            // JWT authentication is stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =========================
                // PUBLIC AUTH ENDPOINTS
                // =========================

                // Candidate registration
                .requestMatchers(
                    "/api/auth/register"
                ).permitAll()

                // Candidate OTP verification
                .requestMatchers(
                    "/api/auth/verify-otp"
                ).permitAll()

                // Candidate resend OTP
                .requestMatchers(
                    "/api/auth/resend-otp"
                ).permitAll()

                // Candidate forgot password
                .requestMatchers(
                    "/api/auth/forgot-password"
                ).permitAll()

                // Candidate verify reset OTP
                .requestMatchers(
                    "/api/auth/verify-reset-otp"
                ).permitAll()

                // Candidate reset password
                .requestMatchers(
                    "/api/auth/reset-password"
                ).permitAll()

                // Login
                .requestMatchers(
                    "/api/auth/login"
                ).permitAll()

                // =========================
                // INTERVIEWER REGISTRATION
                // =========================

                // Interviewer can register without login
                // Admin approval is required afterwards
                .requestMatchers(
                    "/api/auth/interviewer/register"
                ).permitAll()
                
                
                
             
                // HR PUBLIC AUTH APIs
               

                .requestMatchers(
                        "/api/auth/hr/register",
                        "/api/auth/hr/login",
                        "/api/auth/hr/forgot-password",
                        "/api/auth/hr/verify-reset-otp",
                        "/api/auth/hr/reset-password"
                ).permitAll()
                
                
                
                // ADMIN PUBLIC AUTH APIs
                // =========================================

                .requestMatchers(
                        "/api/auth/admin/login",
                        "/api/auth/admin/forgot-password",
                        "/api/auth/admin/verify-reset-otp",
                        "/api/auth/admin/reset-password"
                ).permitAll()
                
                

                // =========================
                // SWAGGER / OPENAPI
                // =========================

                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // =========================
                // ROLE BASED ACCESS
                // =========================

                // Candidate APIs
                .requestMatchers(
                    "/api/candidate/**"
                ).hasRole("CANDIDATE")

                // Interviewer APIs
                .requestMatchers(
                    "/api/interviewer/**"
                ).hasRole("INTERVIEWER")

                // HR APIs
                .requestMatchers(
                    "/api/hr/**"
                ).hasRole("HR")

                // Admin APIs
                .requestMatchers(
                    "/api/admin/**"
                ).hasRole("ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // JWT filter
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}