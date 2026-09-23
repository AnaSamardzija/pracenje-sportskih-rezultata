package rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.MembershipDocument;

import java.util.List;
import java.util.Optional;

public interface MongoMembershipRepository extends MongoRepository<MembershipDocument, String>
{
	List<MembershipDocument> findByGroupId(String groupId);

	List<MembershipDocument> findByUserId(String userId);

	Optional<MembershipDocument> findByUserIdAndGroupId(String userId, String groupId);
}
