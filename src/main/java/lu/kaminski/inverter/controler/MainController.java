package lu.kaminski.inverter.controler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.model.rest.DailyProdResponse;
import lu.kaminski.inverter.model.rest.LiveDataResponse;
import lu.kaminski.inverter.service.DataService;
import lu.kaminski.inverter.service.StatusService;
import lu.kaminski.inverter.service.SyncService;
import lu.kaminski.inverter.util.DateUtil;
import lu.kaminski.inverter.util.NotifUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final NotifUtil notifUtil;
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

    @RequestMapping(value = "/inverter-status", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> checkInverterStatus() {
        log.debug("in checkInverterStatus");
        syncService.checkInverterStatus();
        return ResponseEntity.ok("check has been triggered");
    }

    /**
     * Return records from database
     */
    @RequestMapping(value = "/daily-prod", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getDailyProd(@RequestParam Map<String, String> customQuery) {
        log.debug("in getDailyProd");
        if (!customQuery.containsKey("start") || !customQuery.containsKey("end")) {
            // Will return HTTP Code 405
            log.error("Invalid input [start or end are not defined]");
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(NO_PARAM.getMessage());
        }
        List<DailyProdResponse> test = dataService.getDailyProd(customQuery.get("start"), customQuery.get("end"));
        return ResponseEntity.ok(test);
    }

    /**
     * Will retrieve data from inverter and data from database
     */
    @RequestMapping(value = "/livedata", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getLiveData() {

        try {
            LiveDataResponse test = syncService.getAndSyncLiveData();
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            log.error("Error while getting livedata", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "Solar App 🚨");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * SYNC DATA FROM INVERTER /
     */
    @RequestMapping(value = "/update", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> updateProd(@RequestParam Map<String, String> customQuery) {
        String nbDays = customQuery.getOrDefault("nbDays", "5");
        log.debug("in updateProd");
        syncService.syncProductionData(Long.valueOf(nbDays));
        return ResponseEntity.ok("ok");
    }
}
