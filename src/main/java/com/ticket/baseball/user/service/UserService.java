package com.ticket.baseball.user.service;

import com.ticket.baseball.auth.JwtProvider;
import com.ticket.baseball.exception.BusinessException;
import com.ticket.baseball.exception.ErrorCode;
import com.ticket.baseball.user.dto.UserInfoResponse;
import com.ticket.baseball.user.dto.UserLoginRequest;
import com.ticket.baseball.user.dto.UserLoginResponse;
import com.ticket.baseball.user.dto.UserSignupRequest;
import com.ticket.baseball.user.entity.User;
import com.ticket.baseball.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


// 사용자 관련 비즈니스 로직 처리
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


    // 회원가입 처리
    @Transactional
    public Long signup(UserSignupRequest request) {

        // 1. 로그인 아이디 중복 체크
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USER);
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USER);
        }


        // 3. 비밀번호 암호화 후 회원 객체 생성
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();


        // 4. 회원 저장
        userRepository.save(user);


        // 5. 생성된 회원 ID 반환
        return user.getId();
    }


    // 로그인 처리
    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {

        // 1. 로그인 아이디로 회원 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND));


        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }


        // 3. JWT 생성
        // JWT의 subject에 로그인 아이디 저장
        String accessToken = jwtProvider.createToken(user.getLoginId());


        // 4. 로그인 성공 응답 반환
        return new UserLoginResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                accessToken
        );
    }


    // 현재 로그인한 회원 정보 조회
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo() {

        // 1. SecurityContext에서 로그인한 사용자 로그인 아이디 조회
        String loginId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();


        // 2. 로그인 아이디로 회원 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND));


        // 3. 회원 정보 반환
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}