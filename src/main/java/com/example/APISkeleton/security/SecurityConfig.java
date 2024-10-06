package com.example.APISkeleton.security;

import com.example.APISkeleton.security.exceptions.ExceptionAccessDeniedHandlerImpl;
import com.example.APISkeleton.security.exceptions.ExceptionAuthenticationEntryPointImpl;
import com.example.APISkeleton.security.filters.JWTVerifierFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JWTVerifierFilter jwtVerifierFilter;
    private final ExceptionAccessDeniedHandlerImpl accessDeniedHandler;
    private final ExceptionAuthenticationEntryPointImpl exceptionAuthenticationEntryPoint;

    public SecurityConfig(JWTVerifierFilter jwtVerifierFilter, ExceptionAccessDeniedHandlerImpl accessDeniedHandler, ExceptionAuthenticationEntryPointImpl exceptionAuthenticationEntryPoint) {
        this.jwtVerifierFilter = jwtVerifierFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.exceptionAuthenticationEntryPoint = exceptionAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtVerifierFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(exceptionAuthenticationEntryPoint);
                    exception.accessDeniedHandler(accessDeniedHandler);
                })
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "users").permitAll()
                                .requestMatchers(HttpMethod.POST, "auth/authenticate").permitAll()
                                .requestMatchers(HttpMethod.POST, "auth/refresh-token").permitAll()
                                .anyRequest().authenticated()
                );

        return http.build();
    }
}
