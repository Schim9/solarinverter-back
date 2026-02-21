package lu.kaminski.inverter.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "contract", ignoreUnknownFields = false)
@Getter
@Setter
public class ContractProperties {

    private int anniversaryDay;
    private int anniversaryMonth;
}
