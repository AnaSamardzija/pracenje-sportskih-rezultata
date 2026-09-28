package rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.UserEntity;

import java.util.Optional;

public interface MariaDbUserRepository extends JpaRepository<UserEntity, Long>
{
	Optional<UserEntity> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
