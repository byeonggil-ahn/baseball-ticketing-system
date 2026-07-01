package com.ticket.baseball.user.service;

import com.ticket.baseball.auth.JwtProvider;
import com.ticket.baseball.user.dto.UserLoginRequest;
import com.ticket.baseball.user.dto.UserLoginResponse;
import com.ticket.baseball.user.dto.UserSignupRequest;
import com.ticket.baseball.user.entity.User;
import com.ticket.baseball.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public Long signup(UserSignupRequest request) {

        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. Builder로 객체 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();

        // 3. 저장
        userRepository.save(user);

        return user.getId();
    }

    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {

        // 1. 이메일 존재 여부 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. JWT 생성
        String accessToken = jwtProvider.createToken(user.getEmail());

        // 4. 로그인 성공
        return new UserLoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                accessToken
        );
    }
}