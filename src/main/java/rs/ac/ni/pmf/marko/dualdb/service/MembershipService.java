package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Group;
import rs.ac.ni.pmf.marko.dualdb.model.GroupRole;
import rs.ac.ni.pmf.marko.dualdb.model.MemberView;
import rs.ac.ni.pmf.marko.dualdb.model.Membership;
import rs.ac.ni.pmf.marko.dualdb.model.User;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.marko.dualdb.storage.membership.MembershipStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;

	public void createAdminMembership(final String groupId, final String userId)
	{
		final Membership membership = Membership.builder()
				.userId(userId)
				.groupId(groupId)
				.roleInGroup(GroupRole.GROUP_ADMIN)
				.build();

		membershipStorage().save(membership);
	}

	public void join(final String groupId, final String userId)
	{
		requireGroupExists(groupId);

		if (membershipStorage().findByUserIdAndGroupId(userId, groupId).isPresent())
		{
			throw new DuplicateResourceException("You are already a member of this group");
		}

		final Membership membership = Membership.builder()
				.userId(userId)
				.groupId(groupId)
				.roleInGroup(GroupRole.MEMBER)
				.build();

		membershipStorage().save(membership);
	}

	public void leave(final String groupId, final String userId)
	{
		final Membership membership = membershipStorage().findByUserIdAndGroupId(userId, groupId)
				.orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));

		membershipStorage().deleteById(membership.getId());
	}

	public List<MemberView> listMembers(final String groupId)
	{
		requireGroupExists(groupId);

		final UserStorage users = userStorage();
		return membershipStorage().findByGroupId(groupId).stream()
				.map(membership -> MemberView.builder()
						.userId(membership.getUserId())
						.username(users.findById(membership.getUserId()).map(User::getUsername).orElse(null))
						.roleInGroup(membership.getRoleInGroup())
						.joinedAt(membership.getJoinedAt())
						.build())
				.collect(Collectors.toList());
	}

	public void kick(final String groupId, final String targetUserId, final String currentUserId)
	{
		requireGroupAdmin(groupId, currentUserId);

		final Membership membership = membershipStorage().findByUserIdAndGroupId(targetUserId, groupId)
				.orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));

		membershipStorage().deleteById(membership.getId());
	}

	public void removeAllForGroup(final String groupId)
	{
		final MembershipStorage storage = membershipStorage();
		storage.findByGroupId(groupId).forEach(membership -> storage.deleteById(membership.getId()));
	}

	public GroupRole roleOf(final String groupId, final String userId)
	{
		return membershipStorage().findByUserIdAndGroupId(userId, groupId)
				.map(Membership::getRoleInGroup)
				.orElse(null);
	}

	public void requireGroupAdmin(final String groupId, final String userId)
	{
		if (roleOf(groupId, userId) != GroupRole.GROUP_ADMIN)
		{
			throw new AccessDeniedException("Only a group admin can perform this action");
		}
	}

	private void requireGroupExists(final String groupId)
	{
		groupStorage().findById(groupId)
				.orElseThrow(() -> new ResourceNotFoundException("Group with id " + groupId + " not found"));
	}

	private MembershipStorage membershipStorage()
	{
		return (MembershipStorage) resolve(Membership.class);
	}

	private UserStorage userStorage()
	{
		return (UserStorage) resolve(User.class);
	}

	private DataStorage<Group> groupStorage()
	{
		return resolve(Group.class);
	}

	private <T> DataStorage<T> resolve(final Class<T> dataType)
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return _storageResolver.resolve(storageType, dataType)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
