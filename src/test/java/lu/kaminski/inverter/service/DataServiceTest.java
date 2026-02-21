package lu.kaminski.inverter.service;

import lu.kaminski.inverter.dao.DailyProdDAO;
import lu.kaminski.inverter.model.entity.DailyProdEntity;
import lu.kaminski.inverter.model.rest.ProdRestModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataServiceTest {

    @Mock
    private DailyProdDAO dailyProdDAO;

    @InjectMocks
    private DataService dataService;

    @Test
    void getDailyProd_returnsEmptyList_whenNoData() {
        when(dailyProdDAO.findByDate(any(), any())).thenReturn(Collections.emptyList());

        List<ProdRestModel> result = dataService.getDailyProd("2024-01-01", "2024-01-31");

        assertThat(result).isEmpty();
    }

    @Test
    void getDailyProd_mappsEntityToRestModel() {
        DailyProdEntity entity = new DailyProdEntity();
        entity.setDate(LocalDate.of(2024, 1, 15));
        entity.setValue(new BigDecimal("5.5"));

        when(dailyProdDAO.findByDate(any(), any())).thenReturn(List.of(entity));

        List<ProdRestModel> result = dataService.getDailyProd("2024-01-01", "2024-01-31");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo("2024-01-15");
        assertThat(result.get(0).getValue()).isEqualTo(new BigDecimal("5.5"));
    }

    @Test
    void getDailyProd_returnAllRecords() {
        DailyProdEntity entity1 = new DailyProdEntity();
        entity1.setDate(LocalDate.of(2024, 1, 1));
        entity1.setValue(new BigDecimal("3.0"));

        DailyProdEntity entity2 = new DailyProdEntity();
        entity2.setDate(LocalDate.of(2024, 1, 2));
        entity2.setValue(new BigDecimal("4.5"));

        when(dailyProdDAO.findByDate(any(), any())).thenReturn(List.of(entity1, entity2));

        List<ProdRestModel> result = dataService.getDailyProd("2024-01-01", "2024-01-31");

        assertThat(result).hasSize(2);
    }
}
