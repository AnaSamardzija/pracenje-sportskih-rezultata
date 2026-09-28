package rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.GroupEntity;

public interface MariaDbGroupRepository extends JpaRepository<GroupEntity, Long>
{
}
