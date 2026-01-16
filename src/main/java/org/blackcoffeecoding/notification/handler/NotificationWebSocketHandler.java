package org.blackcoffeecoding.notification.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    // ЗАДАНИЕ 2: Храним сессии по UserID
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Извлекаем userId из URL (ws://.../?userId=user1)
        String userId = getUserIdFromSession(session);

        if (userId != null) {
            sessions.put(userId, session);
            log.info("Подключен пользователь: {}, SessionID: {}", userId, session.getId());
        } else {
            log.warn("Подключение без userId! SessionID: {}", session.getId());
            // Можно закрыть соединение, если ID обязателен
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();

        // ЗАДАНИЕ 1: Ping/Pong
        if ("PING".equals(payload)) {
            // log.debug("PING from {}", session.getId()); // Раскомментируй, если хочешь видеть
            session.sendMessage(new TextMessage("PONG"));
            return;
        }

        log.info("Сообщение от {}: {}", session.getId(), payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("Пользователь {} отключился", userId);
        }
    }

    /**
     * ЗАДАНИЕ 2: Отправка лично в руки
     */
    public boolean sendPrivateMessage(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (IOException e) {
                log.error("Ошибка отправки пользователю {}", userId, e);
            }
        } else {
            log.warn("Пользователь {} не найден или оффлайн", userId);
        }
        return false;
    }

    // Старый метод Broadcast (немного переписанный под Map)
    public int broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        int sentCount = 0;
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                    sentCount++;
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return sentCount;
    }

    public int getActiveConnections() {
        return sessions.size();
    }

    // Вспомогательный метод парсинга URL
    private String getUserIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            // Очень простой парсинг: ищем "userId="
            // В продакшене лучше использовать библиотеку для парсинга query params
            String query = uri.getQuery();
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "userId".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}