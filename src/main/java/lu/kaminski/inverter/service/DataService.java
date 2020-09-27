package lu.kaminski.inverter.service;

import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.dao.DailyProdDAO;
import lu.kaminski.inverter.dao.HourlyProdDAO;
import lu.kaminski.inverter.model.entity.DailyProdEntity;
import lu.kaminski.inverter.model.entity.HourlyProdEntity;
import lu.kaminski.inverter.model.rest.ProdRestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
public class DataService {

    @Autowired
    private DailyProdDAO dailyProdDAO;
    @Autowired
    private HourlyProdDAO hourlyProdDAO;

    public List<ProdRestModel> getDailyProd(String startDate, String endDate) {
        log.debug("getDailyProd from [" + startDate + "] to [" + endDate + "]");
        List<DailyProdEntity> records = dailyProdDAO.findByDate(LocalDate.parse(startDate), LocalDate.parse(endDate));
        List<ProdRestModel> result = records.stream().map(r -> ProdRestModel.builder()
                .date(r.getDate().toString())
                .value(r.getValue())
                .build()).collect(Collectors.toList());
        return result;
    }

    public List<ProdRestModel> getHourlyProd(String day) {
        log.debug("getDailyProd for [" + day + "]");
        List<HourlyProdEntity> records = hourlyProdDAO.findByDate(LocalDateTime.parse(day+ "T00:00:00.000"), LocalDateTime.parse(day + "T23:59:00.000"));
        List<ProdRestModel> result = records.stream().map(r -> ProdRestModel.builder()
                .date(day)
                .time(r.getDate().format(DateTimeFormatter.ISO_LOCAL_TIME))
                .value(r.getValue())
                .build()).collect(Collectors.toList());
        return result;
    }
}
