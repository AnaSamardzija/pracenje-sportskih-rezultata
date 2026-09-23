package rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.GroupDocument;

public interface MongoGroupRepository extends MongoRepository<GroupDocument, String>
{
}
