package lu.kaminski.inverter.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "inverter", ignoreUnknownFields = false)
@Getter
@Setter
public class InverterProperties {

    private String url;

    private String token;
}
