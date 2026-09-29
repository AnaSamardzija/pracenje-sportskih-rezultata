package rs.ac.ni.pmf.ana.dualdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Group;
import rs.ac.ni.pmf.ana.dualdb.model.GroupRole;
import rs.ac.ni.pmf.ana.dualdb.model.MemberView;
import rs.ac.ni.pmf.ana.dualdb.model.Membership;
import rs.ac.ni.pmf.ana.dualdb.model.Permission;
import rs.ac.ni.pmf.ana.dualdb.model.User;
import rs.ac.ni.pmf.ana.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.ana.dualdb.storage.membership.MembershipStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

	@Transactional
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

		log.info("User {} joined group {}", userId, groupId);
		ensureActiveAdmin(groupId);
	}

	@Transactional
	public MemberView addMember(final String groupId, final String username, final User currentUser)
	{
		requireGroupExists(groupId);
		requireGroupAdmin(groupId, currentUser, Permission.GROUPS_MEMBERS_ADD_ANY);

		final User user = userStorage().findByUsername(username.strip())
				.orElseThrow(() -> new ResourceNotFoundException("User '" + username + "' not found"));

		if (!user.isActive())
		{
			throw new InvalidOperationException("User '" + user.getUsername() + "' is deactivated");
		}

		if (membershipStorage().findByUserIdAndGroupId(user.getId(), groupId).isPresent())
		{
			throw new DuplicateResourceException("User '" + user.getUsername() + "' is already a member of this group");
		}

		final Membership membership = Membership.builder()
				.userId(user.getId())
				.groupId(groupId)
				.roleInGroup(GroupRole.MEMBER)
				.build();

		final Membership saved = membershipStorage().save(membership);

		log.info("User {} added to group {} by {}", user.getId(), groupId, currentUser.getId());
		ensureActiveAdmin(groupId);

		return MemberView.builder()
				.userId(saved.getUserId())
				.username(user.getUsername())
				.active(user.isActive())
				.roleInGroup(roleOf(groupId, user.getId()))
				.joinedAt(saved.getJoinedAt())
				.build();
	}

	@Transactional
	public void leave(final String groupId, final String userId)
	{
		final Membership membership = membershipStorage().findByUserIdAndGroupId(userId, groupId)
				.orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));

		if (membership.getRoleInGroup() == GroupRole.GROUP_ADMIN && activeOthers(membership).isEmpty())
		{
			throw new InvalidOperationException("You are the only active member of this group; delete the group instead");
		}

		membershipStorage().deleteById(membership.getId());

		log.info("User {} left group {}", userId, groupId);
		ensureActiveAdmin(groupId);
	}

	/**
	 * Posle deaktivacije ili vraćanja korisnika proverava sve njegove grupe, jer se tada menja ko je u grupi aktivan.
	 */
	@Transactional
	public void ensureActiveAdminInGroupsOf(final String userId)
	{
		membershipStorage().findByUserId(userId)
				.forEach(membership -> ensureActiveAdmin(membership.getGroupId()));
	}

	public List<MemberView> listMembers(final String groupId)
	{
		requireGroupExists(groupId);

		final List<Membership> memberships = membershipStorage().findByGroupId(groupId);
		final Map<String, User> users = usersById(memberships);

		return memberships.stream()
				.map(membership ->
				{
					final Optional<User> user = Optional.ofNullable(users.get(membership.getUserId()));

					return MemberView.builder()
							.userId(membership.getUserId())
							.username(user.map(User::getUsername).orElse(null))
							.active(user.map(User::isActive).orElse(false))
							.roleInGroup(membership.getRoleInGroup())
							.joinedAt(membership.getJoinedAt())
							.build();
				})
				.toList();
	}

	@Transactional
	public void kick(final String groupId, final String targetUserId, final User currentUser)
	{
		requireGroupExists(groupId);
		requireGroupAdmin(groupId, currentUser, Permission.GROUPS_MEMBERS_KICK_ANY);

		final Membership membership = membershipStorage().findByUserIdAndGroupId(targetUserId, groupId)
				.orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));

		if (membership.getUserId().equals(currentUser.getId()))
		{
			throw new InvalidOperationException("You cannot remove yourself from the group; use leave instead");
		}

		membershipStorage().deleteById(membership.getId());

		log.info("User {} removed from group {}", membership.getUserId(), groupId);
		ensureActiveAdmin(groupId);
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

	public Set<String> memberIds(final String groupId)
	{
		return membershipStorage().findByGroupId(groupId).stream()
				.map(Membership::getUserId)
				.collect(Collectors.toSet());
	}

	public long countMembers(final String groupId)
	{
		return membershipStorage().findByGroupId(groupId).size();
	}

	public Map<String, Long> countByGroup()
	{
		return membershipStorage().findAll().stream()
				.collect(Collectors.groupingBy(Membership::getGroupId, Collectors.counting()));
	}

	public Map<String, GroupRole> rolesByUser(final String userId)
	{
		return membershipStorage().findByUserId(userId).stream()
				.collect(Collectors.toMap(Membership::getGroupId, Membership::getRoleInGroup));
	}

	public void requireGroupAdmin(final String groupId, final User currentUser, final Permission anyGroupPermission)
	{
		if (!currentUser.hasPermission(anyGroupPermission)
				&& roleOf(groupId, currentUser.getId()) != GroupRole.GROUP_ADMIN)
		{
			throw new AccessDeniedException("Only a group admin can perform this action");
		}
	}

	private List<Membership> activeOthers(final Membership membership)
	{
		final List<Membership> others = membershipStorage().findByGroupId(membership.getGroupId()).stream()
				.filter(other -> !other.getId().equals(membership.getId()))
				.toList();
		final Set<String> activeUserIds = activeUserIds(others);

		return others.stream()
				.filter(other -> activeUserIds.contains(other.getUserId()))
				.toList();
	}

	private Map<String, User> usersById(final List<Membership> memberships)
	{
		final Set<String> userIds = memberships.stream().map(Membership::getUserId).collect(Collectors.toSet());

		return userStorage().findAllById(userIds).stream()
				.collect(Collectors.toMap(User::getId, Function.identity()));
	}

	private Set<String> activeUserIds(final List<Membership> memberships)
	{
		return usersById(memberships).values().stream()
				.filter(User::isActive)
				.map(User::getId)
				.collect(Collectors.toSet());
	}

	/**
	 * Grupa uvek ima aktivnog GROUP_ADMIN-a dok ima aktivnih članova. Kad ga nema (admin je otišao, izbačen ili
	 * deaktiviran), admin postaje aktivan član koji je najduže u grupi, a neaktivni admini postaju MEMBER.
	 */
	private void ensureActiveAdmin(final String groupId)
	{
		final List<Membership> memberships = membershipStorage().findByGroupId(groupId);
		final Set<String> activeUserIds = activeUserIds(memberships);
		final List<Membership> active = memberships.stream()
				.filter(membership -> activeUserIds.contains(membership.getUserId()))
				.toList();

		if (active.isEmpty() || active.stream().anyMatch(membership -> membership.getRoleInGroup() == GroupRole.GROUP_ADMIN))
		{
			return;
		}

		memberships.stream()
				.filter(membership -> membership.getRoleInGroup() == GroupRole.GROUP_ADMIN)
				.forEach(admin ->
				{
					admin.setRoleInGroup(GroupRole.MEMBER);
					membershipStorage().save(admin);
				});

		final Membership successor = active.stream()
				.min(Comparator.comparing(Membership::getJoinedAt))
				.orElseThrow();

		successor.setRoleInGroup(GroupRole.GROUP_ADMIN);
		membershipStorage().save(successor);

		log.info("User {} is now group admin of group {}", successor.getUserId(), groupId);
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
