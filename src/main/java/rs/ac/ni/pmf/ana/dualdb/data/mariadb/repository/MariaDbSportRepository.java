package rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.SportEntity;

import java.util.List;
import java.util.Optional;

public interface MariaDbSportRepository extends JpaRepository<SportEntity, Long>
{
	List<SportEntity> findByActiveTrue();

	Optional<SportEntity> findByNameIgnoreCase(String name);
}
