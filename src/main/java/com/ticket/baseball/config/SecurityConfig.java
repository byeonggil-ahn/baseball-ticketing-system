package com.ticket.baseball.config;

import com.ticket.baseball.auth.JwtAuthenticationFilter;
import com.ticket.baseball.exception.JwtEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security 설정
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            JwtEntryPoint jwtEntryPoint) throws Exception {

        http
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 인증 없이 접근 가능한 URL
                        .requestMatchers(
                                "/users/signup",
                                "/users/login",
                                "/users/test",
                                "/games",
                                "/payment.html",
                                "/payment-success.html",
                                "/payment-fail.html",
                                "/payments/*/confirm"
                        ).permitAll()

                        // 그 외 모든 요청은 JWT 인증 필요
                        .anyRequest().authenticated()
                )

                // Spring Security 기본 로그인 페이지 비활성화
                .formLogin(form -> form.disable())

                // HTTP Basic 인증 비활성화
                .httpBasic(basic -> basic.disable())

                // JWT 인증 실패 처리
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtEntryPoint)
                );

        // JWT 인증 필터 실행
        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}