package com.ticket.baseball.game.repository;

import com.ticket.baseball.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;


// Game 데이터 접근 Repository
public interface GameRepository extends JpaRepository<Game, Long> {

}