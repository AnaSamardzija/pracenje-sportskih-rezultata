package rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.UserDocument;

import java.util.Optional;

public interface MongoUserRepository extends MongoRepository<UserDocument, String>
{
	Optional<UserDocument> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
