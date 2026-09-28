package rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.RoleEntity;

import java.util.Optional;
import java.util.Set;

public interface MariaDbRoleRepository extends JpaRepository<RoleEntity, Long>
{
	Optional<RoleEntity> findByName(String name);

	boolean existsByName(String role);

	Set<RoleEntity> findAllByNameIn(Set<String> admin);
}
