package rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.SportDocument;

import java.util.List;
import java.util.Optional;

public interface MongoSportRepository extends MongoRepository<SportDocument, String>
{
	List<SportDocument> findByActiveTrue();

	@Collation("{ 'locale': 'en', 'strength': 1 }")
	Optional<SportDocument> findByName(String name);
}
