package lu.kaminski.inverter.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.config.properties.PushBulletProperties;
import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class NotifUtil {

    private final PushBulletProperties pushBulletProperties;

    public void sendPushBulletNotif(String message, String title) {
        log.debug("Send a notification to PushBullet: [" + message + "]");

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder builder;
        try {
            builder = new URIBuilder("https://api.pushbullet.com/v2/pushes");
            URI uri = builder.build();

            HttpPost post = new HttpPost(uri);
            post.addHeader("Access-Token", pushBulletProperties.getApiKey());
            post.addHeader("Content-Type", "application/json");
            String json = new Gson().toJson(Map.of("body", message, "title", title, "type", "note"));
            StringEntity params = new StringEntity(json, StandardCharsets.UTF_8);
            post.setEntity(params);
            httpClient.execute(post);
        } catch (Exception e) {
            log.error("Error while sending push notification", e);
        }
    }
}
