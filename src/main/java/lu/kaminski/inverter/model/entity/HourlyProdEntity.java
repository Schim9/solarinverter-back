package lu.kaminski.inverter.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "hourly_prod")
@Getter
@Setter
public class HourlyProdEntity {

    @Id
    private LocalDateTime date;

    @Column(name = "value")
    private BigDecimal value;
}

