package com.ticket.baseball.user.controller;

import com.ticket.baseball.user.dto.UserInfoResponse;
import com.ticket.baseball.user.dto.UserLoginRequest;
import com.ticket.baseball.user.dto.UserLoginResponse;
import com.ticket.baseball.user.dto.UserSignupRequest;
import com.ticket.baseball.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


// 회원 관련 API 처리
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    // 서버 동작 확인용 테스트 API
    @GetMapping("/test")
    public String test() {
        return "success";
    }


    // 회원가입 API
    @PostMapping("/signup")
    public Long signup(
            @Valid @RequestBody UserSignupRequest request) {

        return userService.signup(request);
    }


    // 로그인 API
    @PostMapping("/login")
    public UserLoginResponse login(
            @RequestBody UserLoginRequest request) {

        return userService.login(request);
    }

    // 현재 로그인한 회원 정보 조회 API
    @GetMapping("/myinfo")
    public UserInfoResponse getMyInfo() {
        return userService.getMyInfo();
    }
}