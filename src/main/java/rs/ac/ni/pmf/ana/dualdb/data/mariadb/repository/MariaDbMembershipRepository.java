package rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.MembershipEntity;

import java.util.List;
import java.util.Optional;

public interface MariaDbMembershipRepository extends JpaRepository<MembershipEntity, Long>
{
	List<MembershipEntity> findByGroup_Id(Long groupId);

	List<MembershipEntity> findByUser_Id(Long userId);

	Optional<MembershipEntity> findByUser_IdAndGroup_Id(Long userId, Long groupId);
}
