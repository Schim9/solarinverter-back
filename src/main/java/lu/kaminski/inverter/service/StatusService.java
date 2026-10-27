package lu.kaminski.inverter.service;

import lombok.RequiredArgsConstructor;
import lu.kaminski.inverter.common.CommonData;
import lu.kaminski.inverter.util.DateUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lu.kaminski.inverter.common.Messages.Message.*;


@Component
@RequiredArgsConstructor
public class StatusService {

    private final CommonData commonData;

    public List<String> getAnomalies() {
        var result = new ArrayList<String>();
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);

        commonData.getLastInverterPolling()
                .filter(t -> t.isBefore(threshold))
                .map(t -> NO_SYNC_SINCE.getMessage(t.format(DateUtil.FORMAT)))
                .ifPresent(result::add);

        commonData.getLastInverterSyncData()
                .filter(t -> t.isBefore(threshold))
                .map(t -> NO_DATA_SYNC_SINCE.getMessage(t.format(DateUtil.FORMAT)))
                .ifPresent(result::add);

        return result;
    }

    public List<String> getInfos() {
        var result = new ArrayList<String>();

        commonData.getLastInverterPolling()
                .map(t -> LAST_INVERTER_POLLING.getMessage(t.format(DateUtil.FORMAT)))
                .ifPresent(result::add);

        commonData.getLastInverterSyncData()
                .map(t -> LAST_INVERTER_SYNC_DATA.getMessage(t.format(DateUtil.FORMAT)))
                .ifPresent(result::add);

        return result;
    }
}