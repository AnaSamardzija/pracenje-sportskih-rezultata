package rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.RoleDocument;

import java.util.Optional;
import java.util.Set;

public interface MongoRoleRepository extends MongoRepository<RoleDocument, String>
{
	@Collation("{ 'locale': 'en', 'strength': 1 }")
	Optional<RoleDocument> findByName(String name);

	@Collation("{ 'locale': 'en', 'strength': 1 }")
	Set<RoleDocument> findAllByNameIn(Set<String> names);
}
