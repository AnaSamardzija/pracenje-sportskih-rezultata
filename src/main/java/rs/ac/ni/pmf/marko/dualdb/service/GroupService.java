package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Group;
import rs.ac.ni.pmf.marko.dualdb.model.GroupDetails;
import rs.ac.ni.pmf.marko.dualdb.model.GroupRole;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;
	private final MembershipService _membershipService;
	private final MatchService _matchService;

	public List<GroupDetails> findAll(final boolean mine, final String search, final String currentUserId)
	{
		List<Group> groups = storage().findAll();

		if (search != null && !search.isBlank())
		{
			final String query = search.toLowerCase();
			groups = groups.stream()
					.filter(group -> group.getName() != null && group.getName().toLowerCase().contains(query))
					.collect(Collectors.toList());
		}

		final Map<String, GroupRole> myRoles = _membershipService.rolesByUser(currentUserId);

		if (mine)
		{
			groups = groups.stream()
					.filter(group -> myRoles.containsKey(group.getId()))
					.collect(Collectors.toList());
		}

		final Map<String, Long> counts = _membershipService.countByGroup();

		return groups.stream()
				.map(group -> GroupDetails.builder()
						.group(group)
						.memberCount(counts.getOrDefault(group.getId(), 0L))
						.myRole(myRoles.get(group.getId()))
						.build())
				.collect(Collectors.toList());
	}

	public GroupDetails findById(final String id, final String currentUserId)
	{
		return toDetails(loadGroup(id), currentUserId);
	}

	@Transactional
	public GroupDetails create(final Group group, final String currentUserId)
	{
		group.setCreatedBy(currentUserId);
		final Group saved = storage().save(group);

		_membershipService.createAdminMembership(saved.getId(), currentUserId);

		return toDetails(saved, currentUserId);
	}

	public GroupDetails update(final String id, final Group group, final String currentUserId)
	{
		final Group existing = loadGroup(id);
		_membershipService.requireGroupAdmin(id, currentUserId);

		existing.setName(group.getName());
		existing.setDescription(group.getDescription());

		return toDetails(storage().save(existing), currentUserId);
	}

	@Transactional
	public void delete(final String id, final String currentUserId)
	{
		loadGroup(id);
		_membershipService.requireGroupAdmin(id, currentUserId);

		if (_matchService.hasMatches(id))
		{
			throw new InvalidOperationException("Group with id " + id + " has recorded matches and cannot be deleted");
		}

		_membershipService.removeAllForGroup(id);
		storage().deleteById(id);
	}

	private Group loadGroup(final String id)
	{
		return storage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Group with id " + id + " not found"));
	}

	private GroupDetails toDetails(final Group group, final String currentUserId)
	{
		return GroupDetails.builder()
				.group(group)
				.memberCount(_membershipService.countMembers(group.getId()))
				.myRole(_membershipService.roleOf(group.getId(), currentUserId))
				.build();
	}

	private DataStorage<Group> storage()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return _storageResolver.resolve(storageType, Group.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
