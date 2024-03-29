package lu.kaminski.inverter.service;

import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.dao.DailyProdDAO;
import lu.kaminski.inverter.dao.HourlyProdDAO;
import lu.kaminski.inverter.model.entity.DailyProdEntity;
import lu.kaminski.inverter.model.entity.HourlyProdEntity;
import lu.kaminski.inverter.model.rest.ProdRestModel;
import lu.kaminski.inverter.util.NotifUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Component
public class SyncService {

    @Autowired
    private DailyProdDAO dailyProdDAO;
    @Autowired
    private HourlyProdDAO hourlyProdDAO;
    @Autowired
    private InverterService inverterService;
    @Autowired
    private NotifUtil notifUtil;

    @Scheduled(cron = "${schedule.task.syncProductionData}")
    public void schedulerSyncDataForPreviousDays() {
        log.info("[SCHEDULER] Sync data for previous days");
        syncProductionData(5L);
    }

    @Scheduled(cron = "${schedule.task.checkInverterStatus}")
    public void schedulerCheckInverterStatus() {
        log.info("[SCHEDULER] Check inverter status");
        checkInverterStatus();
    }

    public void checkInverterStatus() {
        log.info("Checking inverter status");
        try {
            ProdRestModel liveData = inverterService.getLiveData();
            // Send a notification in case nothing has been produced
            if (liveData.getDayProd().equals(BigDecimal.ZERO)) {
                notifUtil.sendPushBulletNotif("Production is near 0", "WARNING");
            }
            else {
                notifUtil.sendPushBulletNotif("The inverter is online", "INFO");
            }
        } catch (Exception e) {
            log.error("Error during checking inverter status", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "ERROR");
            notifUtil.sendPushBulletNotif("Something is wrong with the inverter", "WARNING");
        }
    }

    public void syncProductionData(Long nbDays ) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(nbDays);

        log.info("Sync data from [" + startDate + "] to [" + endDate + " ]");
        try {
            List<ProdRestModel> list = inverterService
                    .getDailyProd(startDate + "T00:00:00.000Z",
                            endDate + "T23:59:00.000Z");
            List<DailyProdEntity> result = list.stream().map(p -> {
                DailyProdEntity dpe = new DailyProdEntity();
                dpe.setDate(LocalDate.parse(p.getDate()));
                dpe.setValue(p.getValue());

                // Send a notification in case nothing has been produced
                if (p.getValue().equals(BigDecimal.ZERO)) {
                    notifUtil.sendPushBulletNotif("No production for " + p.getDate(), "WARNING");
                }
                if (p.getValue().compareTo(BigDecimal.ONE) < 0) {
                    notifUtil.sendPushBulletNotif("Production is low for " + p.getDate(), "WARNING");
                }

                return dpe;
            }).collect(Collectors.toList());
            dailyProdDAO.saveAll(result);
            notifUtil.sendPushBulletNotif("DailyProd Data synchronized", "INFO");
        } catch (Exception e) {
            log.error("Error during sync for dailyProd", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "ERROR");
        }
    }

    // @Scheduled(cron = "${schedule.task.syncProductionDataForOneDay}")
    public void syncProductionDataForOneDay() {
        // Will get data for the day before
        LocalDate day = LocalDate.now().minusDays(1);

        log.info("Sync data for [" + day + "]");
        try {
            List<ProdRestModel> list = inverterService
                    .getProdForDay(day.toString());
            List<HourlyProdEntity> result = list.stream().map(p -> {
                HourlyProdEntity hpe = new HourlyProdEntity();
                hpe.setDate(LocalDateTime.parse(p.getDate()+ "T" +p.getTime()));
                hpe.setValue(p.getValue());
                return hpe;
            }).collect(Collectors.toList());
            hourlyProdDAO.saveAll(result);
            notifUtil.sendPushBulletNotif("HourlyProd Data synchronized", "INFO");
        } catch (Exception e) {
            log.error("Error during sync for hourlyProd", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "ERROR");
        }
    }

    public ProdRestModel getAndSyncLiveData() {
        try {
            // Get live data
            ProdRestModel liveData = inverterService.getLiveData();

            // Get latest data for current day
            Optional<DailyProdEntity> storedCurrentDayProd = dailyProdDAO.findByDate(LocalDate.now(), LocalDate.now())
                    .stream()
                    .findFirst();

            // If there is some value stored in DB, we compare it to the up to date received data
            storedCurrentDayProd.ifPresent(currentDayProd -> {
                if (Optional.ofNullable(liveData.getDayProd())
                        .map(p -> p.compareTo(currentDayProd.getValue()) == 0)
                        .orElse(false)) {
                    notifUtil.sendPushBulletNotif("Production did not change since last sync.", "WARNING");
                }
            });

            // Process live data
            Optional.ofNullable(liveData.getDayProd()).ifPresentOrElse( upToDateData -> {
                // Update production data for current day
                DailyProdEntity dpe = new DailyProdEntity();
                dpe.setDate(LocalDate.now());
                dpe.setValue(upToDateData);
                dailyProdDAO.save(dpe);
            }, () -> {
                // No refreshed data... Return the one retrieved from database in case it exists
                liveData.setValue(
                        storedCurrentDayProd
                                .map(DailyProdEntity::getValue)
                                .orElse(BigDecimal.ZERO)
                );
            });

            return liveData;
        } catch (Exception e) {
            log.error("Error while getting livedata", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "ERROR");
        }
        // In case there is no data, the function returns an empty object
        return new ProdRestModel();
    }
}
