package rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.SportEntity;

public interface MariaDbSportRepository extends JpaRepository<SportEntity, Long>
{
}
