package rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.MatchEntity;

public interface MariaDbMatchRepository extends JpaRepository<MatchEntity, Long>
{
}
