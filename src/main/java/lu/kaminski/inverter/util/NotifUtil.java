package lu.kaminski.inverter.util;

import lombok.extern.log4j.Log4j2;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;
import java.nio.charset.Charset;

@Log4j2
public class NotifUtil {

    public static void sendPushBulletNotif(String message, String title) {
        log.debug("Send a notification to PushBullet: [" + message + "]");
        String apiKey = "o.yB7cAS6FLG97cZMOtnHxotKufYHgqEoe";

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder builder = null;
        try {
            builder = new URIBuilder("https://api.pushbullet.com/v2/pushes");
            URI uri = builder.build();

            HttpPost post = new HttpPost(uri);
            post.addHeader("Access-Token", apiKey);
            post.addHeader("Content-Type", "application/json");
            StringEntity params =new StringEntity("{\"body\":\""+message+"\", " +
                    "\"info\":\"" + title + "\"," +
                    "\"type\":\"note\"} ", Charset.forName("utf-8"));
            post.setEntity(params);
            httpClient.execute(post);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
