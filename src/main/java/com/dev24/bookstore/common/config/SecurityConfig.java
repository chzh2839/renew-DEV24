package com.dev24.bookstore.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dev24.bookstore.auth.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

// URL 패턴(아래 filterChain)은 인증 여부만 가르는 안전망이고, 실제로 어떤 권한이 필요한지는
// 각 컨트롤러 메서드에 붙인 @PreAuthorize가 자기 문서화한다
// (이중 레이어 - URL 패턴은 바뀌어도 어노테이션이 최후 방어선 역할을 한다)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PERMIT_ALL_PATHS = {
            "/api/auth/customers/signup",
            "/api/auth/customers/login",
            "/api/auth/admins/login",
            "/api/auth/refresh",
            "/api/books/**",
            "/api/reviews/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            // 실제 노출 범위를 정하는 건 이 URL 패턴이 아니라 application.properties의
            // management.endpoints.web.exposure.include(명시적 allowlist)다 - 외부에서는 nginx가
            // /actuator/ 전체를 여전히 차단한다(docs/OBSERVABILITY.md 참고).
            "/actuator/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Stateless REST API이므로 CSRF는 불필요
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // HttpSession 미생성
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}