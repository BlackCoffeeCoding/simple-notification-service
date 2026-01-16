package org.blackcoffeecoding.notification.config;

import org.blackcoffeecoding.notification.handler.NotificationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationHandler notificationHandler;

    public WebSocketConfig(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Регистрируем наш хендлер по адресу /ws/notifications
        registry.addHandler(notificationHandler, "/ws/notifications")
                .setAllowedOrigins("*"); // Разрешаем подключение с любых доменов (для тестов)
    }
}