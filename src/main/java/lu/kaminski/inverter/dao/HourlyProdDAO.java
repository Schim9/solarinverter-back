package lu.kaminski.inverter.dao;

import lu.kaminski.inverter.model.entity.HourlyProdEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface HourlyProdDAO extends CrudRepository<HourlyProdEntity, Long> {

    @Query("SELECT p FROM HourlyProdEntity p " +
            "WHERE (p.date) >= :startDate " +
            "AND (p.date) <= :endDate " +
            "ORDER BY p.date DESC")
    List<HourlyProdEntity> findByDate(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
}
