package com.ticket.baseball.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.baseball.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}