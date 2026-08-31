package com.ticket.baseball.queue;

import com.ticket.baseball.exception.BusinessException;
import com.ticket.baseball.exception.ErrorCode;
import com.ticket.baseball.user.entity.User;
import com.ticket.baseball.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final UserRepository userRepository;

    // 현재 JWT 로그인 사용자 조회
    private User getCurrentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

    // 대기열 입장
    @PostMapping("/enter")
    public void enterQueue(
            @RequestParam Long gameId
    ) {

        User user = getCurrentUser();

        queueService.enterQueue(
                gameId,
                user.getId()
        );
    }

    // 대기열 순번 조회
    @GetMapping("/position")
    public Long getQueuePosition(
            @RequestParam Long gameId
    ) {

        User user = getCurrentUser();

        return queueService.getQueuePosition(
                gameId,
                user.getId()
        );
    }

    // 대기열에서 사용자 제거
    @DeleteMapping("/leave")
    public void removeFromQueue(
            @RequestParam Long gameId
    ) {

        User user = getCurrentUser();

        queueService.removeFromQueue(
                gameId,
                user.getId()
        );
    }

    // 해당 경기의 대기열 첫 번째 사용자 처리
    @PostMapping("/process")
    public String processQueue(
            @RequestParam Long gameId
    ) {

        return queueService.processQueue(gameId);
    }
}