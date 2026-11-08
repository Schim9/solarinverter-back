package lu.kaminski.inverter.util;

import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.config.properties.PushBulletProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Log4j2
@Component
public class NotifUtil {

    private final RestClient restClient;

    public NotifUtil(PushBulletProperties pushBulletProperties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.pushbullet.com/v2")
                .defaultHeader("Access-Token", pushBulletProperties.getApiKey())
                .build();
    }

    public void sendPushBulletNotif(String message, String title) {
        log.debug("Send a notification to PushBullet: [" + message + "]");
        try {
            restClient.post()
                    .uri("/pushes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("body", message, "title", title, "type", "note"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Error while sending push notification", e);
        }
    }
}
