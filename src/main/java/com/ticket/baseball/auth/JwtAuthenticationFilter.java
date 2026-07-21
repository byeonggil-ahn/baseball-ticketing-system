package com.ticket.baseball.auth;

import com.ticket.baseball.exception.JwtEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final JwtEntryPoint jwtEntryPoint;

    // JwtProvider, JwtEntryPoint 주입
    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            JwtEntryPoint jwtEntryPoint) {

        this.jwtProvider = jwtProvider;
        this.jwtEntryPoint = jwtEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더 조회
        String authHeader = request.getHeader("Authorization");

        // JWT가 없으면 다음 필터로 이동
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 제거 후 토큰 추출
        String token = authHeader.substring(7);

        // JWT가 유효하지 않으면 인증 실패 처리
        if (!jwtProvider.validateToken(token)) {
            jwtEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException("유효하지 않은 JWT입니다.")
            );
            return;
        }

        // JWT에서 사용자 이메일 추출
        String email = jwtProvider.getEmailFromToken(token);

        // 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.emptyList()
                );

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}