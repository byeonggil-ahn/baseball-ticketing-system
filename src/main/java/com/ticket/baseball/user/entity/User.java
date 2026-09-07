package com.ticket.baseball.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    // 사용자 고유 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디
    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    // 비밀번호
    @Column(nullable = false)
    private String password;

    // 닉네임
    @Column(nullable = false, length = 20)
    private String nickname;

    // 이메일
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    // 사용자 권한
    @Column(nullable = false, length = 20)
    private String role;

    // 가입 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;


    // User 객체 생성
    @Builder
    public User(String loginId,
                String password,
                String nickname,
                String email,
                String role,
                LocalDateTime createdAt) {

        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }
}