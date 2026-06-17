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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    private LocalDateTime createdAt;

    @Builder
    public User(String email, String password, String name, LocalDateTime createdAt) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.createdAt = createdAt;
    }
}