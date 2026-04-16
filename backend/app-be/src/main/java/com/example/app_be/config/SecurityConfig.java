package com.example.app_be.config;

import com.example.app_be.core.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(
                                                authorize -> authorize
                                                                .requestMatchers(
                                                                                "/api/v1/auth/login",
                                                                                "/api/v1/health",
                                                                                "/error",
                                                                                "/api/v1/users/no-auth",
                                                                                "/api/v1/orders",
                                                                                "/api/v1/orders/*",
                                                                                "/ws-order/**"
                                                                // ,"/api/v1/users"
                                                                ).permitAll()
                                                                .requestMatchers(HttpMethod.GET,
                                                                                "/api/v1/products/**",
                                                                                "/api/v1/catalogs/**",
                                                                                "/api/v1/saleoffs/**",
                                                                                "/api/v1/product-images/**",
                                                                                "/api/v1/orders/code/**")
                                                                .permitAll()
                                                                .requestMatchers(
                                                                                "/api/v1/products/**",
                                                                                "/api/v1/catalogs/**",
                                                                                "/api/v1/saleoffs/**",
                                                                                "/api/v1/product-images/**")
                                                                .hasRole("MANAGER")
                                                                .requestMatchers(
                                                                                "/api/v1/orders/pending",
                                                                                "/api/v1/orders/*/paid",
                                                                                "/api/v1/orders/*/status",
                                                                                "/api/v1/orders/*/assign",
                                                                                "/api/v1/orders/**")
                                                                .hasAnyRole("STAFF", "MANAGER")
                                                                .requestMatchers("/api/v1/statistics/**")
                                                                .hasRole("MANAGER")
                                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}
