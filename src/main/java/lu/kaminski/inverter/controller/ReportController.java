package lu.kaminski.inverter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.model.rest.DailyProdResponse;
import lu.kaminski.inverter.service.DataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ReportController {

    private final DataService dataService;

    @GetMapping({"/" , "/report"})
    public String report(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {

        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();

        List<DailyProdResponse> dailyProd = dataService.getDailyProd(start.toString(), end.toString())
                .stream()
                .sorted(Comparator.comparing(DailyProdResponse::getDate))
                .toList();
        List<String> labels = dailyProd.stream().map(DailyProdResponse::getDate).toList();
        List<BigDecimal> values = dailyProd.stream().map(DailyProdResponse::getValue).toList();

        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartValues", values);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        return "report";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public RedirectView handleBadDateParam() {
        log.warn("Invalid date parameter — redirecting to default report");
        return new RedirectView("/report");
    }
}
