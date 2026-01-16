package org.blackcoffeecoding.notification.controller;

import org.blackcoffeecoding.notification.handler.NotificationWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationWebSocketHandler handler;

    public NotificationController(NotificationWebSocketHandler handler) {
        this.handler = handler;
    }

    // Общая рассылка (старое)
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcast(@RequestBody String message) {
        int count = handler.broadcast(message);
        return ResponseEntity.ok(Map.of("sentTo", count));
    }

    // ЗАДАНИЕ 2: Личное сообщение
    // POST http://localhost:8086/api/notifications/private
    // Body (JSON): { "userId": "user1", "message": "Секретно!" }
    @PostMapping("/private")
    public ResponseEntity<Map<String, Object>> sendPrivate(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String message = payload.get("message");

        boolean success = handler.sendPrivateMessage(userId, message);

        if (success) {
            return ResponseEntity.ok(Map.of("status", "sent", "to", userId));
        } else {
            return ResponseEntity.status(404).body(Map.of("status", "error", "reason", "User not found or offline"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of("activeUsers", handler.getActiveConnections()));
    }
}