package rs.ac.ni.pmf.marko.dualdb.storage.membership;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.GroupEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.MembershipEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.mapper.MariaDbMembershipMapper;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbGroupRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbMembershipRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.marko.dualdb.model.Membership;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MariaDbMembershipStorage extends MembershipStorage
{
	private final MariaDbMembershipRepository _membershipRepository;
	private final MariaDbUserRepository _userRepository;
	private final MariaDbGroupRepository _groupRepository;
	private final MariaDbMembershipMapper _membershipMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MARIADB;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Membership> findAll()
	{
		return _membershipRepository.findAll().stream()
				.map(_membershipMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Membership> findById(final String id)
	{
		return _membershipRepository.findById(Long.parseLong(id)).map(_membershipMapper::toModel);
	}

	@Override
	@Transactional
	public Membership save(final Membership membership)
	{
		final MembershipEntity entity = membership.getId() == null ? createNew(membership) : update(membership);
		return _membershipMapper.toModel(_membershipRepository.save(entity));
	}

	private MembershipEntity createNew(final Membership membership)
	{
		final UserEntity user = _userRepository.getReferenceById(Long.parseLong(membership.getUserId()));
		final GroupEntity group = _groupRepository.getReferenceById(Long.parseLong(membership.getGroupId()));
		return _membershipMapper.toEntity(membership, user, group);
	}

	private MembershipEntity update(final Membership membership)
	{
		final MembershipEntity existing = _membershipRepository.findById(Long.parseLong(membership.getId()))
				.orElseThrow(() -> new IllegalArgumentException("Membership with id " + membership.getId() + " not found"));

		existing.setRoleInGroup(membership.getRoleInGroup());

		return existing;
	}

	@Override
	@Transactional
	public void deleteById(final String id)
	{
		_membershipRepository.deleteById(Long.parseLong(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Membership> findByGroupId(final String groupId)
	{
		return _membershipRepository.findByGroup_Id(Long.parseLong(groupId)).stream()
				.map(_membershipMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Membership> findByUserId(final String userId)
	{
		return _membershipRepository.findByUser_Id(Long.parseLong(userId)).stream()
				.map(_membershipMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Membership> findByUserIdAndGroupId(final String userId, final String groupId)
	{
		return _membershipRepository
				.findByUser_IdAndGroup_Id(Long.parseLong(userId), Long.parseLong(groupId))
				.map(_membershipMapper::toModel);
	}
}
