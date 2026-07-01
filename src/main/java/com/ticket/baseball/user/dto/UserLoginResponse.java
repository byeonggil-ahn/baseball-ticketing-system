package com.ticket.baseball.user.dto;

public class UserLoginResponse {

    private Long id;
    private String name;
    private String email;
    private String accessToken;

    public UserLoginResponse() {
    }

    public UserLoginResponse(Long id, String name, String email, String accessToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }
}