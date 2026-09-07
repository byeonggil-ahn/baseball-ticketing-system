package com.ticket.baseball.user.repository;

import com.ticket.baseball.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


// User 데이터 접근 Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 아이디로 사용자 조회
    Optional<User> findByLoginId(String loginId);

    // 로그인 아이디 중복 확인
    boolean existsByLoginId(String loginId);

    // 이메일 중복 확인
    boolean existsByEmail(String email);
}