package lu.kaminski.inverter.model.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "daily_prod")
@Getter
@Setter
public class DailyProdEntity {

    @Id
    private LocalDate date;

    @Column(name = "value")
    private BigDecimal value;
}

