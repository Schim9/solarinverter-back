package lu.kaminski.inverter.model.rest;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProdResponse {

    private String date;
    private BigDecimal value;
}
