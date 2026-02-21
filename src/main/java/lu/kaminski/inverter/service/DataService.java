package lu.kaminski.inverter.service;

import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.dao.DailyProdDAO;
import lu.kaminski.inverter.model.entity.DailyProdEntity;
import lu.kaminski.inverter.model.rest.ProdRestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
public class DataService {

    @Autowired
    private DailyProdDAO dailyProdDAO;

    public List<ProdRestModel> getDailyProd(String startDate, String endDate) {
        log.debug("getDailyProd from [" + startDate + "] to [" + endDate + "]");
        List<DailyProdEntity> records = dailyProdDAO.findByDate(LocalDate.parse(startDate), LocalDate.parse(endDate));
        List<ProdRestModel> result = records.stream().map(r -> ProdRestModel.builder()
                .date(r.getDate().toString())
                .value(r.getValue())
                .build()).collect(Collectors.toList());
        return result;
    }
}
