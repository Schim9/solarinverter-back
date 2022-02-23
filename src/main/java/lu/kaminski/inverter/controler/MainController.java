package lu.kaminski.inverter.controler;

import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.model.rest.ProdRestModel;
import lu.kaminski.inverter.service.DataService;
import lu.kaminski.inverter.service.SyncService;
import lu.kaminski.inverter.util.NotifUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static lu.kaminski.inverter.common.Messages.Message.NOT_IMPLEMENTED;
import static lu.kaminski.inverter.common.Messages.Message.NO_PARAM;

@Log4j2
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api")
public class MainController {

    @Autowired
    private NotifUtil notifUtil;
    @Autowired
    private DataService dataService;
    @Autowired
    private SyncService syncService;

    /**
     * GET /
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<?> status() {
        log.debug("in status");
        return ResponseEntity.ok("App is up on " + LocalDateTime.now() + ".");
    }

    /**
     * GET /
     */
    @RequestMapping(value = "/daily-prod", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getDailyProd(@RequestParam Map<String, String> customQuery) {
        log.debug("in getDailyProd");
        if (!customQuery.containsKey("start") || !customQuery.containsKey("end")) {
            // Will return HTTP Code 405
            log.error("Invalid input [start or end are not defined]");
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(NO_PARAM.getMessage());
        }
        List<ProdRestModel> test = dataService.getDailyProd(customQuery.get("start"), customQuery.get("end"));
        return ResponseEntity.ok(test);
    }

    /**
     * GET /
     */
    @RequestMapping(value = "/real-time", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getRealTimeProd(@RequestParam Map<String, String> customQuery) {
        log.debug("in getRealTimeProd");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(NOT_IMPLEMENTED.getMessage());
    }

    /**
     * GET /
     */
    @RequestMapping(value = "/livedata", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getLiveData() {

        try {
            ProdRestModel test = syncService.getAndSyncLiveData();
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            log.error("Error while getting livedata", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * GET /
     */
    @RequestMapping(value = "/update", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> updateProd(@RequestParam Map<String, String> customQuery) {
        String nbDays = customQuery.getOrDefault("nbDays", "5");
        log.debug("in updateProd");

        syncService.syncProductionData(Long.valueOf(nbDays));
        return ResponseEntity.ok("ok");
    }


}
