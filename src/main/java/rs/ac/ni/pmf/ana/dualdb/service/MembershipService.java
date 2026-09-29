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
				.roleInGroup(roleForNewMember(groupId, userId))
				.build();

		membershipStorage().save(membership);

		log.info("User {} joined group {}", userId, groupId);
	}

	@Transactional
	public MemberView addMember(final String groupId, final String username, final User currentUser)
	{
		requireGroupExists(groupId);
		requireGroupAdmin(groupId, currentUser, Permission.GROUPS_MEMBERS_ADD_ANY);

		final User user = userStorage().findByUsername(username)
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
				.roleInGroup(roleForNewMember(groupId, user.getId()))
				.build();

		final Membership saved = membershipStorage().save(membership);

		log.info("User {} added to group {} by {}", user.getId(), groupId, currentUser.getId());

		return MemberView.builder()
				.userId(saved.getUserId())
				.username(user.getUsername())
				.active(user.isActive())
				.roleInGroup(saved.getRoleInGroup())
				.joinedAt(saved.getJoinedAt())
				.build();
	}

	@Transactional
	public void leave(final String groupId, final String userId)
	{
		final Membership membership = membershipStorage().findByUserIdAndGroupId(userId, groupId)
				.orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));

		if (membership.getRoleInGroup() == GroupRole.GROUP_ADMIN)
		{
			final List<Membership> others = activeOthers(membership);

			if (others.isEmpty())
			{
				throw new InvalidOperationException("You are the only active member of this group; delete the group instead");
			}

			keepGroupAdmin(groupId, others);
		}

		membershipStorage().deleteById(membership.getId());

		log.info("User {} left group {}", userId, groupId);
	}

	/**
	 * Deaktiviran korisnik predaje ulogu GROUP_ADMIN u svakoj grupi u kojoj ima aktivnih članova i tamo
	 * postaje MEMBER. U grupi bez drugih aktivnih članova ostaje admin, jer nema kome da je preda.
	 */
	@Transactional
	public void handOverGroupAdmin(final String userId)
	{
		final List<Membership> adminMemberships = membershipStorage().findByUserId(userId).stream()
				.filter(membership -> membership.getRoleInGroup() == GroupRole.GROUP_ADMIN)
				.toList();

		for (final Membership membership : adminMemberships)
		{
			final List<Membership> others = activeOthers(membership);

			if (!others.isEmpty())
			{
				membership.setRoleInGroup(GroupRole.MEMBER);
				membershipStorage().save(membership);

				keepGroupAdmin(membership.getGroupId(), others);
			}
		}
	}

	public List<MemberView> listMembers(final String groupId)
	{
		requireGroupExists(groupId);

		final UserStorage users = userStorage();
		return membershipStorage().findByGroupId(groupId).stream()
				.map(membership ->
				{
					final Optional<User> user = users.findById(membership.getUserId());

					return MemberView.builder()
							.userId(membership.getUserId())
							.username(user.map(User::getUsername).orElse(null))
							.active(user.map(User::isActive).orElse(false))
							.roleInGroup(membership.getRoleInGroup())
							.joinedAt(membership.getJoinedAt())
							.build();
				})
				.collect(Collectors.toList());
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

		if (membership.getRoleInGroup() == GroupRole.GROUP_ADMIN)
		{
			final List<Membership> others = activeOthers(membership);

			if (!others.isEmpty())
			{
				keepGroupAdmin(groupId, others);
			}
		}

		membershipStorage().deleteById(membership.getId());

		log.info("User {} removed from group {}", membership.getUserId(), groupId);
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
		return membershipStorage().findByGroupId(membership.getGroupId()).stream()
				.filter(other -> !other.getId().equals(membership.getId()))
				.filter(this::isActive)
				.toList();
	}

	private boolean isActive(final Membership membership)
	{
		return userStorage().findById(membership.getUserId()).map(User::isActive).orElse(false);
	}

	/**
	 * Grupa u kojoj nijedan GROUP_ADMIN nije aktivan (admin je deaktiviran ili izbačen, a tada nije imao kome da
	 * preda ulogu) dobija za admina prvog novog člana, a deaktivirani admin postaje MEMBER, kao pri predaji uloge.
	 */
	private GroupRole roleForNewMember(final String groupId, final String userId)
	{
		final List<Membership> admins = membershipStorage().findByGroupId(groupId).stream()
				.filter(membership -> membership.getRoleInGroup() == GroupRole.GROUP_ADMIN)
				.toList();

		if (admins.stream().anyMatch(this::isActive))
		{
			return GroupRole.MEMBER;
		}

		admins.forEach(admin ->
		{
			admin.setRoleInGroup(GroupRole.MEMBER);
			membershipStorage().save(admin);
		});

		log.info("User {} is now group admin of group {}", userId, groupId);
		return GroupRole.GROUP_ADMIN;
	}

	/**
	 * Grupa uvek ima aktivnog GROUP_ADMIN-a dok ima aktivnih članova. Kad admin odlazi ili je deaktiviran,
	 * a među ostalim aktivnim članovima nema admina, admin postaje onaj koji je najduže u grupi.
	 */
	private void keepGroupAdmin(final String groupId, final List<Membership> others)
	{
		if (others.stream().anyMatch(other -> other.getRoleInGroup() == GroupRole.GROUP_ADMIN))
		{
			return;
		}

		final Membership successor = others.stream()
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
