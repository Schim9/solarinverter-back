package lu.kaminski.inverter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.common.CommonData;
import lu.kaminski.inverter.config.properties.ContractProperties;
import lu.kaminski.inverter.model.entity.DailyProdEntity;
import lu.kaminski.inverter.model.rest.DailyProdResponse;
import lu.kaminski.inverter.model.rest.LiveDataResponse;
import lu.kaminski.inverter.util.NotifUtil;
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
@RequiredArgsConstructor
public class SyncService {

    private final InverterService inverterService;
    private final NotifUtil notifUtil;
    private final DataService dataService;
    private final ContractProperties contractProperties;
    private final CommonData commonData;

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
            LiveDataResponse liveData = inverterService.getLiveData();
            commonData.setLastScheduledPoll(LocalDateTime.now());
            // Send a notification in case nothing has been produced
            if (BigDecimal.ZERO.equals(liveData.getDayProd())) {
                notifUtil.sendPushBulletNotif("Production is near 0", "Solar App ⚠️");
            }
        } catch (Exception e) {
            log.error("Error during checking inverter status", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "Solar App 🚨");
        }
    }

    public void syncProductionData(Long nbDays ) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(nbDays);

        log.info("Sync data from [" + startDate + "] to [" + endDate + " ]");
        try {
            List<DailyProdResponse> list = inverterService
                    .getDailyProd(startDate + "T00:00:00.000Z",
                            endDate + "T23:59:00.000Z");
            List<DailyProdEntity> result = list.stream().map((DailyProdResponse p) -> {
                DailyProdEntity dpe = new DailyProdEntity();
                dpe.setDate(LocalDate.parse(p.getDate()));
                dpe.setValue(p.getValue());
                return dpe;
            }).collect(Collectors.toList());
            dataService.saveAll(result);
            commonData.setLastInverterSyncData(LocalDateTime.now());

            list.forEach(p -> {
                if (p.getValue().equals(BigDecimal.ZERO)) {
                    notifUtil.sendPushBulletNotif("No production for " + p.getDate(), "Solar App ⚠️");
                }
                if (p.getValue().compareTo(BigDecimal.ONE) < 0) {
                    notifUtil.sendPushBulletNotif("Production is low for " + p.getDate(), "Solar App ️ ⚠️️");
                }
            });
        } catch (Exception e) {
            log.error("Error during sync for dailyProd", e);
            notifUtil.sendPushBulletNotif(e.getMessage(), "Solar App 🚨");
        }
    }

    LocalDate getLastAnniversaryDate(LocalDate today) {
        LocalDate anniversaryThisYear = LocalDate.of(today.getYear(),
                contractProperties.getAnniversaryMonth(),
                contractProperties.getAnniversaryDay());
        return anniversaryThisYear.isAfter(today)
                ? anniversaryThisYear.minusYears(1)
                : anniversaryThisYear;
    }

    public LiveDataResponse getAndSyncLiveData() {
        try {
            // Get live data
            LiveDataResponse liveData = inverterService.getLiveData();
            commonData.setLastLiveDataPoll(LocalDateTime.now());
            // Get latest data for current day
            Optional<DailyProdEntity> storedCurrentDayProd = dataService.findForDate(LocalDate.now());

            // If there is some value stored in DB, we compare it to the up to date received data
            storedCurrentDayProd.ifPresent(currentDayProd -> {
                if (Optional.ofNullable(liveData.getDayProd())
                        .map(p -> p.compareTo(currentDayProd.getValue()) == 0)
                        .orElse(false)) {
                    notifUtil.sendPushBulletNotif("Production did not change since last sync.", "Solar App ⚠️");
                }
            });

            // Process live data
            Optional.ofNullable(liveData.getDayProd()).ifPresentOrElse( upToDateData -> {
                // Update production data for current day
                DailyProdEntity dpe = new DailyProdEntity();
                dpe.setDate(LocalDate.now());
                dpe.setValue(upToDateData);
                dataService.save(dpe);
            }, () -> {
                // No refreshed data... Return the one retrieved from database in case it exists
                liveData.setDayProd(
                        storedCurrentDayProd
                                .map(DailyProdEntity::getValue)
                                .orElse(BigDecimal.ZERO)
                );
            });

            // Add production since contract anniversary date
            LocalDate anniversaryDate = getLastAnniversaryDate(LocalDate.now());
            liveData.setContractProd(dataService.getProductionSince(anniversaryDate));
            liveData.setContractStartDate(anniversaryDate.toString());
            log.debug("Contract production since [" + anniversaryDate + "]: " + liveData.getContractProd());

            return liveData;
        } catch (Exception e) {
            log.warn("Inverter unreachable during live data request: {}", e.getMessage());
        }
        // In case there is no data, the function returns an empty object
        return new LiveDataResponse();
    }
}
