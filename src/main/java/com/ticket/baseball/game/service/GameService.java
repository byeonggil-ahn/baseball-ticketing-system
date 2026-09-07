package com.ticket.baseball.game.service;

import com.ticket.baseball.game.dto.GameCreateRequest;
import com.ticket.baseball.game.dto.GameResponse;
import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


// 경기 관련 비즈니스 로직 처리
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;


    // 경기 등록
    public GameResponse createGame(GameCreateRequest request) {

        // DTO -> Entity 변환
        Game game = Game.builder()
                .homeTeam(request.getHomeTeam())
                .awayTeam(request.getAwayTeam())
                .stadium(request.getStadium())
                .gameDate(request.getGameDate())
                .reservationStartAt(request.getReservationStartAt())
                .reservationEndAt(request.getReservationEndAt())
                .createdAt(LocalDateTime.now())
                .build();

        // 저장 후 응답 DTO 반환
        return new GameResponse(gameRepository.save(game));
    }


    // 경기 전체 조회
    public List<GameResponse> getGames() {

        // Entity -> DTO 변환
        return gameRepository.findAll()
                .stream()
                .map(GameResponse::new)
                .toList();
    }


    // 경기 상세 조회
    public GameResponse getGame(Long id) {

        // ID로 경기 조회
        Game game = gameRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("경기를 찾을 수 없습니다.")
                );

        // Entity -> DTO 변환
        return new GameResponse(game);
    }
}