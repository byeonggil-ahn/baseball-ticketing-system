package com.ticket.baseball.game.controller;

import com.ticket.baseball.game.dto.GameCreateRequest;
import com.ticket.baseball.game.dto.GameResponse;
import com.ticket.baseball.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// 경기 API 요청 처리
@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;


    // 경기 등록
    @PostMapping
    public GameResponse createGame(
            @RequestBody GameCreateRequest request) {

        return gameService.createGame(request);
    }


    // 경기 전체 조회
    @GetMapping
    public List<GameResponse> getGames() {

        return gameService.getGames();
    }


    // 경기 상세 조회
    @GetMapping("/{gameId}")
    public GameResponse getGame(
            @PathVariable Long gameId) {

        return gameService.getGame(gameId);
    }
}