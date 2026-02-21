package lu.kaminski.inverter.service;

import lu.kaminski.inverter.config.properties.ContractProperties;
import lu.kaminski.inverter.dao.DailyProdDAO;
import lu.kaminski.inverter.model.entity.DailyProdEntity;
import lu.kaminski.inverter.model.rest.ProdRestModel;
import lu.kaminski.inverter.util.NotifUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private DailyProdDAO dailyProdDAO;

    @Mock
    private InverterService inverterService;

    @Mock
    private NotifUtil notifUtil;

    @Mock
    private DataService dataService;

    @Mock
    private ContractProperties contractProperties;

    @InjectMocks
    private SyncService syncService;

    @BeforeEach
    void setUp() {
        // Default contract anniversary: 25 January (lenient: not all tests use contractProperties)
        lenient().when(contractProperties.getAnniversaryDay()).thenReturn(25);
        lenient().when(contractProperties.getAnniversaryMonth()).thenReturn(1);
    }

    // --- getLastAnniversaryDate ---

    @Test
    void getLastAnniversaryDate_returnsThisYear_whenAnniversaryAlreadyPassed() {
        // Today is 21 Feb 2026, anniversary is 25 Jan → already passed → 2026-01-25
        LocalDate result = syncService.getLastAnniversaryDate(LocalDate.of(2026, 2, 21));

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 25));
    }

    @Test
    void getLastAnniversaryDate_returnsLastYear_whenAnniversaryNotYetReached() {
        // Today is 10 Jan 2026, anniversary is 25 Jan → not yet reached → 2025-01-25
        LocalDate result = syncService.getLastAnniversaryDate(LocalDate.of(2026, 1, 10));

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 25));
    }

    @Test
    void getLastAnniversaryDate_returnsToday_whenTodayIsAnniversary() {
        // Today IS the anniversary date → return this year's date
        LocalDate result = syncService.getLastAnniversaryDate(LocalDate.of(2026, 1, 25));

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 25));
    }

    @Test
    void getLastAnniversaryDate_handlesEndOfYear() {
        // Today is 31 Dec 2026, anniversary is 25 Jan → already passed → 2026-01-25
        LocalDate result = syncService.getLastAnniversaryDate(LocalDate.of(2026, 12, 31));

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 25));
    }

    @Test
    void getLastAnniversaryDate_handlesStartOfYear() {
        // Today is 1 Jan 2026, anniversary is 25 Jan → not yet reached → 2025-01-25
        LocalDate result = syncService.getLastAnniversaryDate(LocalDate.of(2026, 1, 1));

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 25));
    }

    // --- checkInverterStatus ---

    @Test
    void checkInverterStatus_sendsWarning_whenDayProdIsZero() {
        ProdRestModel liveData = ProdRestModel.builder().dayProd(BigDecimal.ZERO).build();
        when(inverterService.getLiveData()).thenReturn(liveData);

        syncService.checkInverterStatus();

        verify(notifUtil).sendPushBulletNotif("Production is near 0", "WARNING");
    }

    @Test
    void checkInverterStatus_sendsInfo_whenInverterOnline() {
        ProdRestModel liveData = ProdRestModel.builder().dayProd(new BigDecimal("3.5")).build();
        when(inverterService.getLiveData()).thenReturn(liveData);

        syncService.checkInverterStatus();

        verify(notifUtil).sendPushBulletNotif("The inverter is online", "INFO");
    }

    @Test
    void checkInverterStatus_sendsError_whenExceptionThrown() {
        when(inverterService.getLiveData()).thenThrow(new RuntimeException("Inverter connection failed"));

        syncService.checkInverterStatus();

        verify(notifUtil).sendPushBulletNotif(any(), eq("ERROR"));
        verify(notifUtil).sendPushBulletNotif("Something is wrong with the inverter", "WARNING");
    }

    // --- syncProductionData ---

    @Test
    void syncProductionData_savesAllRecords() throws Exception {
        ProdRestModel prod = ProdRestModel.builder()
                .date(LocalDate.now().toString())
                .value(new BigDecimal("5.0"))
                .build();
        when(inverterService.getDailyProd(any(), any())).thenReturn(List.of(prod));

        syncService.syncProductionData(5L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DailyProdEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(dailyProdDAO).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getValue()).isEqualTo(new BigDecimal("5.0"));
    }

    @Test
    void syncProductionData_sendsWarning_whenZeroProduction() throws Exception {
        ProdRestModel prod = ProdRestModel.builder()
                .date(LocalDate.now().toString())
                .value(BigDecimal.ZERO)
                .build();
        when(inverterService.getDailyProd(any(), any())).thenReturn(List.of(prod));

        syncService.syncProductionData(5L);

        verify(notifUtil).sendPushBulletNotif(contains("No production"), eq("WARNING"));
    }

    @Test
    void syncProductionData_sendsWarning_whenLowProduction() throws Exception {
        ProdRestModel prod = ProdRestModel.builder()
                .date(LocalDate.now().toString())
                .value(new BigDecimal("0.5"))
                .build();
        when(inverterService.getDailyProd(any(), any())).thenReturn(List.of(prod));

        syncService.syncProductionData(5L);

        verify(notifUtil).sendPushBulletNotif(contains("Production is low"), eq("WARNING"));
    }

    @Test
    void syncProductionData_sendsError_onException() throws Exception {
        when(inverterService.getDailyProd(any(), any())).thenThrow(new Exception("network error"));

        syncService.syncProductionData(5L);

        verify(notifUtil).sendPushBulletNotif(any(), eq("ERROR"));
        verify(dailyProdDAO, never()).saveAll(any());
    }

    // --- getAndSyncLiveData ---

    @Test
    void getAndSyncLiveData_savesCurrentDayData() {
        BigDecimal dayProd = new BigDecimal("4.2");
        ProdRestModel liveData = ProdRestModel.builder().dayProd(dayProd).build();
        when(inverterService.getLiveData()).thenReturn(liveData);
        when(dailyProdDAO.findByDate(any(), any())).thenReturn(Collections.emptyList());

        ProdRestModel result = syncService.getAndSyncLiveData();

        verify(dailyProdDAO).save(any(DailyProdEntity.class));
        assertThat(result.getDayProd()).isEqualTo(dayProd);
    }

    @Test
    void getAndSyncLiveData_usesDbValue_whenDayProdIsNull() {
        ProdRestModel liveData = ProdRestModel.builder().build(); // dayProd is null
        when(inverterService.getLiveData()).thenReturn(liveData);

        DailyProdEntity storedEntity = new DailyProdEntity();
        storedEntity.setDate(LocalDate.now());
        storedEntity.setValue(new BigDecimal("3.5"));
        when(dailyProdDAO.findByDate(any(), any())).thenReturn(List.of(storedEntity));

        ProdRestModel result = syncService.getAndSyncLiveData();

        assertThat(result.getValue()).isEqualTo(new BigDecimal("3.5"));
        verify(dailyProdDAO, never()).save(any());
    }

    @Test
    void getAndSyncLiveData_sendsWarning_whenProductionStagnates() {
        BigDecimal sameValue = new BigDecimal("5.0");
        ProdRestModel liveData = ProdRestModel.builder().dayProd(sameValue).build();
        when(inverterService.getLiveData()).thenReturn(liveData);

        DailyProdEntity storedEntity = new DailyProdEntity();
        storedEntity.setDate(LocalDate.now());
        storedEntity.setValue(sameValue);
        when(dailyProdDAO.findByDate(any(), any())).thenReturn(List.of(storedEntity));

        syncService.getAndSyncLiveData();

        verify(notifUtil).sendPushBulletNotif("Production did not change since last sync.", "WARNING");
    }

    @Test
    void getAndSyncLiveData_returnsEmptyModel_onException() {
        when(inverterService.getLiveData()).thenThrow(new RuntimeException("error"));

        ProdRestModel result = syncService.getAndSyncLiveData();

        assertThat(result).isNotNull();
        assertThat(result.getDayProd()).isNull();
    }
}
