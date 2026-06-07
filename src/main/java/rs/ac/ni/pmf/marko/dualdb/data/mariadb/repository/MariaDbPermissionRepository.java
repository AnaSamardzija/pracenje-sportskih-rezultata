package rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.PermissionEntity;

import java.util.Optional;

public interface MariaDbPermissionRepository extends JpaRepository<PermissionEntity, Long>
{
	Optional<PermissionEntity> findByName(String name);

	boolean existsByName(String permission);
}
