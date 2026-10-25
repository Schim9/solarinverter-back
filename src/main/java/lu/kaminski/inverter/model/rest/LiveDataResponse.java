package lu.kaminski.inverter.model.rest;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveDataResponse {

    private String date;
    private BigDecimal dayProd;
    private BigDecimal weekProd;
    private BigDecimal monthProd;
    private BigDecimal yearProd;
    private BigDecimal contractProd;
    private String contractStartDate;
}
