package com.ticket.baseball.user.service;

import org.springframework.stereotype.Service;

import com.ticket.baseball.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public void signup() {
    	
    }
}