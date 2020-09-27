package lu.kaminski.inverter.dao;

import lu.kaminski.inverter.model.entity.DailyProdEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Transactional
public interface DailyProdDAO extends CrudRepository<DailyProdEntity, Long> {

	List<DailyProdEntity> findAll();

	@Query("SELECT p FROM DailyProdEntity p " +
			"WHERE (p.date) >= :startDate " +
			"AND (p.date) <= :endDate " +
			"ORDER BY p.date DESC")
	List<DailyProdEntity> findByDate(@Param("startDate") LocalDate startDate,
									 @Param("endDate") LocalDate endDate);
}
