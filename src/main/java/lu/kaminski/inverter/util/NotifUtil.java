package lu.kaminski.inverter.util;

import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.config.properties.PushBulletProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Log4j2
@Component
public class NotifUtil {

    @Autowired
    public PushBulletProperties pushBulletProperties;

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
            StringEntity params =new StringEntity("{\"body\":\""+message+"\", " +
                    "\"info\":\"" + title + "\"," +
                    "\"type\":\"note\"} ", StandardCharsets.UTF_8);
            post.setEntity(params);
            httpClient.execute(post);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
