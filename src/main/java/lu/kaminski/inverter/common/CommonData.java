package lu.kaminski.inverter.common;

import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Setter
public class CommonData {

    private volatile LocalDateTime lastScheduledPoll;
    private volatile LocalDateTime lastLiveDataPoll;
    private volatile LocalDateTime lastInverterSyncData;

    public Optional<LocalDateTime> getLastScheduledPoll() {
        return Optional.ofNullable(lastScheduledPoll);
    }

    public Optional<LocalDateTime> getLastLiveDataPoll() {
        return Optional.ofNullable(lastLiveDataPoll);
    }

    public Optional<LocalDateTime> getLastInverterSyncData() {
        return Optional.ofNullable(lastInverterSyncData);
    }
}
