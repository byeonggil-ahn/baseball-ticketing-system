package com.ticket.baseball.user.controller;

import com.ticket.baseball.user.dto.UserLoginRequest;
import com.ticket.baseball.user.dto.UserSignupRequest;
import com.ticket.baseball.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public String test() {
        return "success";
    }

    @PostMapping("/signup")
    public Long signup(@RequestBody UserSignupRequest request) {
        return userService.signup(request);
    }
    
    @PostMapping("/login")
    public Long login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }
}