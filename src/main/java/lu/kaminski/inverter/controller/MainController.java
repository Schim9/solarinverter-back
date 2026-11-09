package lu.kaminski.inverter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.model.rest.DailyProdResponse;
import lu.kaminski.inverter.service.DataService;
import lu.kaminski.inverter.service.StatusService;
import lu.kaminski.inverter.service.SyncService;
import lu.kaminski.inverter.util.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static lu.kaminski.inverter.common.Messages.Message.NO_PARAM;

@Log4j2
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class MainController {

    private final DataService dataService;
    private final SyncService syncService;
    private final DateUtil dateUtil;
    private final StatusService statusService;

    /**
     * Health Check and status /
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<?> status() {
        log.debug("in status");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("appCode", "SOLAR_APP");
        body.put("status", "up");
        body.put("uptime", dateUtil.formatUptime());
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("infos", statusService.getInfos());
        body.put("anomalies", statusService.getAnomalies());
        return ResponseEntity.ok(body);
    }

    /**
     * Return records from database
     */
    @RequestMapping(value = "/daily-prod", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getDailyProd(@RequestParam Map<String, String> customQuery) {
        log.debug("in getDailyProd");
        if (!customQuery.containsKey("start") || !customQuery.containsKey("end")) {
            // Will return HTTP Code 400
            log.error("Invalid input [start or end are not defined]");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NO_PARAM.getMessage());
        }
        List<DailyProdResponse> test = dataService.getDailyProd(customQuery.get("start"), customQuery.get("end"));
        return ResponseEntity.ok(test);
    }

    /**
     * Will retrieve data from inverter and data from database
     */
    @RequestMapping(value = "/livedata", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getLiveData() {
        return ResponseEntity.ok(syncService.getAndSyncLiveData());
    }

    /**
     * SYNC DATA FROM INVERTER /
     */
    @RequestMapping(value = "/update", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> updateProd(@RequestParam(defaultValue = "5") Long nbDays) {
        log.debug("in updateProd");
        syncService.syncProductionData(nbDays);
        return ResponseEntity.ok("ok");
    }
}
