package rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.PermissionDocument;

import java.util.Optional;

public interface MongoPermissionRepository extends MongoRepository<PermissionDocument, String>
{
	@Collation("{ 'locale': 'en', 'strength': 1 }")
	Optional<PermissionDocument> findByName(String name);
}
