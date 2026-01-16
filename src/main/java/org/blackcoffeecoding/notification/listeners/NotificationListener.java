package org.blackcoffeecoding.notification.listeners;

// Импортируем наше событие из JAR
import org.blackcoffeecoding.device.events.DeviceRatedEvent;
import org.blackcoffeecoding.notification.handler.NotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationHandler notificationHandler;

    public NotificationListener(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    // Создаем свою уникальную очередь для браузерных уведомлений
                    value = @Queue(name = "q.notifications.browser", durable = "true"),
                    // Подключаемся к ТОМУ ЖЕ Fanout обменнику, что и аудит
                    exchange = @Exchange(name = "analytics-fanout", type = "fanout")
            )
    )
    public void handleUserRatedEvent(DeviceRatedEvent event) {
        log.info("RABBIT: Получено событие рейтинга: {}", event);

        // Формируем JSON вручную (или можно через ObjectMapper), чтобы отправить в браузер
        // Фронтенд ждет поля: type, userId (deviceId), score, verdict
        String browserMessage = String.format(
                "{\"type\": \"RATING_UPDATE\", \"userId\": %d, \"score\": %d, \"verdict\": \"%s\"}",
                event.deviceId(), event.score(), event.verdict()
        );

        // Рассылаем всем подключенным браузерам
        notificationHandler.broadcast(browserMessage);
    }
}