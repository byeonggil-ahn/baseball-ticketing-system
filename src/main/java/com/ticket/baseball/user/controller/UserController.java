package com.ticket.baseball.user.controller;

import com.ticket.baseball.user.dto.UserInfoResponse;
import com.ticket.baseball.user.dto.UserLoginRequest;
import com.ticket.baseball.user.dto.UserLoginResponse;
import com.ticket.baseball.user.dto.UserSignupRequest;
import com.ticket.baseball.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 테스트용
    @GetMapping("/test")
    public String test() {
        return "success";
    }

    // 회원가입
    @PostMapping("/signup")
    public Long signup(
            @Valid @RequestBody UserSignupRequest request
    ) {
        return userService.signup(request);
    }

    // 로그인
    @PostMapping("/login")
    public UserLoginResponse login(
            @RequestBody UserLoginRequest request,
            HttpServletResponse response
    ) {

        UserLoginResponse loginResponse =
                userService.login(request);

        // JWT를 HttpOnly Cookie에 저장
        Cookie cookie = new Cookie(
                "accessToken",
                loginResponse.getAccessToken()
        );

        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 HTTP 테스트용
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간

        response.addCookie(cookie);

        return loginResponse;
    }

    // 현재 로그인한 사용자 정보
    @GetMapping("/myinfo")
    public UserInfoResponse getMyInfo() {
        return userService.getMyInfo();
    }
}