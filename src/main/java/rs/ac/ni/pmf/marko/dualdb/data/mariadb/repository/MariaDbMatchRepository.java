package rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.MatchEntity;

import java.util.List;

public interface MariaDbMatchRepository extends JpaRepository<MatchEntity, Long>
{
	@Query("""
			select m from MatchEntity m
			where (:groupId is null or m.group.id = :groupId)
			and (:sportId is null or m.sport.id = :sportId)
			and (:playerId is null or exists (
				select s.id from MatchSideEntity s join s.players p
				where s.match = m and p.id = :playerId))
			order by m.playedAt desc, m.id desc
			""")
	List<MatchEntity> search(@Param("groupId") Long groupId,
	                         @Param("sportId") Long sportId,
	                         @Param("playerId") Long playerId);
}
