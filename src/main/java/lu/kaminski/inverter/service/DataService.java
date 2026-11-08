package lu.kaminski.inverter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.dao.DailyProdDAO;
import lu.kaminski.inverter.model.entity.DailyProdEntity;
import lu.kaminski.inverter.model.rest.DailyProdResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Log4j2
@RequiredArgsConstructor
public class DataService {

    private final DailyProdDAO dailyProdDAO;

    public List<DailyProdResponse> getDailyProd(String startDate, String endDate) {
        log.debug("getDailyProd from [" + startDate + "] to [" + endDate + "]");
        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            log.error("Invalid date parameter: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid date format, expected YYYY-MM-DD");
        }
        List<DailyProdEntity> records = dailyProdDAO.findByDate(start, end);
        return records.stream().map(r -> DailyProdResponse.builder()
                .date(r.getDate().toString())
                .value(r.getValue())
                .build()).collect(Collectors.toList());
    }

    public BigDecimal getProductionSince(LocalDate from) {
        log.debug("getProductionSince [" + from + "]");
        return dailyProdDAO.findByDate(from, LocalDate.now()).stream()
                .map(DailyProdEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<DailyProdEntity> findForDate(LocalDate date) {
        return dailyProdDAO.findByDate(date, date).stream().findFirst();
    }

    public void save(DailyProdEntity entity) {
        dailyProdDAO.save(entity);
    }

    public void saveAll(List<DailyProdEntity> entities) {
        dailyProdDAO.saveAll(entities);
    }
}
