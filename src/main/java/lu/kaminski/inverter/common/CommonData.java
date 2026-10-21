package lu.kaminski.inverter.common;

import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Setter
public class CommonData {

    private LocalDateTime lastInverterPolling;
    private LocalDateTime lastInverterSyncData;

    public Optional<LocalDateTime> getLastInverterPolling() {
        return Optional.ofNullable(lastInverterPolling);
    }

    public Optional<LocalDateTime> getLastInverterSyncData() {
        return Optional.ofNullable(lastInverterSyncData);
    }
}
