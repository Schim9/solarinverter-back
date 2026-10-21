package lu.kaminski.inverter.service;

import lu.kaminski.inverter.common.CommonData;
import lu.kaminski.inverter.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lu.kaminski.inverter.common.Messages.Message.NO_DATA_SYNC_SINCE;
import static lu.kaminski.inverter.common.Messages.Message.NO_SYNC_SINCE;

@Component
public class StatusService {

    @Autowired
    private CommonData commonData;

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
}