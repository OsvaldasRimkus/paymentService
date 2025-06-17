package lt.rimkus.paymentService.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static lt.rimkus.paymentService.messages.OtherMessages.FAILURE;
import static lt.rimkus.paymentService.messages.OtherMessages.SUCCESS;

@Service
public class NotificationServiceOne implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceOne.class);

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationServiceOne(RestTemplateBuilder restTemplateBuilder, @Value("${app.geo.service.url:https://api.github.com/users/}") String notificationServiceUrl) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        this.notificationServiceUrl = notificationServiceUrl;
    }

    @Async("notificationExecutor")
    public CompletableFuture<String> notifyServiceAsync(String userName) {
        String url = notificationServiceUrl + userName;
        logger.debug("Calling notification service: {}", url);
        String status = null;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                status = SUCCESS;
            }
        } catch (RestClientException e) {
            logger.error("Error notifying service - {}", e.getMessage());
            status = FAILURE;
        }
        return CompletableFuture.completedFuture(status);
    }
}
