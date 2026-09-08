package com.ticket.baseball.auth;

import com.ticket.baseball.exception.JwtEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// JWT 인증 처리
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final JwtEntryPoint jwtEntryPoint;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            JwtEntryPoint jwtEntryPoint) {

        this.jwtProvider = jwtProvider;
        this.jwtEntryPoint = jwtEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = null;

        // 1. Authorization 헤더 확인
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. Authorization 헤더가 없으면 Cookie 확인
        if (token == null && request.getCookies() != null) {

            for (Cookie cookie : request.getCookies()) {

                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3. JWT가 없으면 다음 필터로 이동
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. JWT 유효성 검사
        if (!jwtProvider.validateToken(token)) {

            jwtEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException(
                            "유효하지 않은 JWT입니다."
                    )
            );

            return;
        }

        // 5. JWT에서 로그인 아이디 추출
        String loginId = jwtProvider.getLoginIdFromToken(token);

        // 6. 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        loginId,
                        null,
                        Collections.emptyList()
                );

        // 7. SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // 8. 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}