package rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.UserDocument;

import java.util.Optional;

public interface MongoUserRepository extends MongoRepository<UserDocument, String>
{
	@Collation("{ 'locale': 'en', 'strength': 1 }")
	Optional<UserDocument> findByUsername(String username);

	@Collation("{ 'locale': 'en', 'strength': 1 }")
	boolean existsByUsername(String username);

	@Collation("{ 'locale': 'en', 'strength': 1 }")
	boolean existsByEmail(String email);
}
