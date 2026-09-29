package rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.MatchDocument;

public interface MongoMatchRepository extends MongoRepository<MatchDocument, String>
{
	boolean existsByGroupId(String groupId);

	boolean existsBySportId(String sportId);
}
