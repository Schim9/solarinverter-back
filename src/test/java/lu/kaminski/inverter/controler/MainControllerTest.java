package lu.kaminski.inverter.controler;

import lu.kaminski.inverter.model.rest.ProdRestModel;
import lu.kaminski.inverter.service.DataService;
import lu.kaminski.inverter.service.SyncService;
import lu.kaminski.inverter.util.NotifUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotifUtil notifUtil;

    @MockBean
    private DataService dataService;

    @MockBean
    private SyncService syncService;

    @Test
    void status_returns200() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk());
    }

    @Test
    void getDailyProd_returns200_withValidParams() throws Exception {
        when(dataService.getDailyProd(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/daily-prod")
                        .param("start", "2024-01-01")
                        .param("end", "2024-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    void getDailyProd_returns405_whenMissingParams() throws Exception {
        mockMvc.perform(get("/api/daily-prod"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getLiveData_returns200() throws Exception {
        when(syncService.getAndSyncLiveData()).thenReturn(new ProdRestModel());

        mockMvc.perform(get("/api/livedata"))
                .andExpect(status().isOk());
    }

    @Test
    void updateProd_returns200() throws Exception {
        mockMvc.perform(get("/api/update").param("nbDays", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void checkInverterStatus_returns200() throws Exception {
        mockMvc.perform(get("/api/inverter-status"))
                .andExpect(status().isOk());
    }
}
