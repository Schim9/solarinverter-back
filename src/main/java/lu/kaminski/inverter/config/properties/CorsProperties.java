package lu.kaminski.inverter.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "cors", ignoreUnknownFields = false)
@Getter
@Setter
public class CorsProperties {

    private List<String> allowedOrigins;
}
