package lu.kaminski.inverter.dao;

import lu.kaminski.inverter.model.entity.DailyProdEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Transactional
public interface DailyProdDAO extends CrudRepository<DailyProdEntity, Long> {

	@Query("SELECT p FROM DailyProdEntity p " +
			"WHERE (p.date) >= :startDate " +
			"AND (p.date) <= :endDate " +
			"ORDER BY p.date DESC")
	List<DailyProdEntity> findByDate(@Param("startDate") LocalDate startDate,
									 @Param("endDate") LocalDate endDate);
}
