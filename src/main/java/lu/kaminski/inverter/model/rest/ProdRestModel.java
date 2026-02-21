package lu.kaminski.inverter.model.rest;

import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class ProdRestModel {

    //Stats
    private String date;
    private String time;
    private BigDecimal value;

    //Livedata
    private BigDecimal dayProd;
    private BigDecimal weekProd;
    private BigDecimal monthProd;
    private BigDecimal yearProd;
    private BigDecimal contractProd;
    private String contractStartDate;
}
