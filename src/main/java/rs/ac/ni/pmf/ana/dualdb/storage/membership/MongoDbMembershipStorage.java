package rs.ac.ni.pmf.ana.dualdb.storage.membership;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.MembershipDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper.MongoMembershipMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoMembershipRepository;
import rs.ac.ni.pmf.ana.dualdb.model.Membership;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoDbMembershipStorage extends MembershipStorage
{
	private final MongoMembershipRepository _membershipRepository;
	private final MongoMembershipMapper _membershipMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MONGODB;
	}

	@Override
	public List<Membership> findAll()
	{
		return _membershipRepository.findAll().stream()
				.map(_membershipMapper::toModel)
				.toList();
	}

	@Override
	public Optional<Membership> findById(final String id)
	{
		return _membershipRepository.findById(id).map(_membershipMapper::toModel);
	}

	@Override
	public Membership save(final Membership membership)
	{
		final MembershipDocument saved = _membershipRepository.save(_membershipMapper.toDocument(membership));
		return _membershipMapper.toModel(saved);
	}

	@Override
	public void deleteById(final String id)
	{
		_membershipRepository.deleteById(id);
	}

	@Override
	public List<Membership> findByGroupId(final String groupId)
	{
		return _membershipRepository.findByGroupId(groupId).stream()
				.map(_membershipMapper::toModel)
				.toList();
	}

	@Override
	public List<Membership> findByUserId(final String userId)
	{
		return _membershipRepository.findByUserId(userId).stream()
				.map(_membershipMapper::toModel)
				.toList();
	}

	@Override
	public Optional<Membership> findByUserIdAndGroupId(final String userId, final String groupId)
	{
		return _membershipRepository.findByUserIdAndGroupId(userId, groupId).map(_membershipMapper::toModel);
	}
}
