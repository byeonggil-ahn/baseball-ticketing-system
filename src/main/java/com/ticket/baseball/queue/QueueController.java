package com.ticket.baseball.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    // 대기열 입장
    @PostMapping("/enter")
    public void enterQueue(@RequestParam Long userId) {
        queueService.enterQueue(userId);
    }

    // 대기열 순번 조회
    @GetMapping("/position")
    public Long getQueuePosition(@RequestParam Long userId) {
        return queueService.getQueuePosition(userId);
    }

    // 대기열에서 사용자 제거
    @DeleteMapping("/leave")
    public void removeFromQueue(@RequestParam Long userId) {
        queueService.removeFromQueue(userId);
    }

    // 대기열 첫 번째 사용자 처리
    @PostMapping("/process")
    public String processQueue() {
        return queueService.processQueue();
    }
}