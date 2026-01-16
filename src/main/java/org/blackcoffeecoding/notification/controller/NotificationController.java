package org.blackcoffeecoding.notification.controller;

import org.blackcoffeecoding.notification.handler.NotificationHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationHandler handler;

    public NotificationController(NotificationHandler handler) {
        this.handler = handler;
    }

    /**
     * Ручная отправка уведомления всем (для тестов)
     * POST http://localhost:8083/api/notifications/broadcast
     * Body: "Привет!"
     */
    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@RequestBody String message) {
        handler.broadcast(message);
        return ResponseEntity.ok("Сообщение отправлено всем активным клиентам");
    }

    /**
     * Статистика подключений
     * GET http://localhost:8083/api/notifications/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
                "activeConnections", handler.getActiveConnections(),
                "service", "Notification Service"
        ));
    }
}