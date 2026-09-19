package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Group;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;

	public List<Group> findAll(final String search)
	{
		final List<Group> groups = storage().findAll();

		if (search == null || search.isBlank())
		{
			return groups;
		}

		final String query = search.toLowerCase();
		return groups.stream()
				.filter(group -> group.getName() != null && group.getName().toLowerCase().contains(query))
				.collect(Collectors.toList());
	}

	public Group findById(final String id)
	{
		return storage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Group with id " + id + " not found"));
	}

	public Group create(final Group group, final String currentUserId)
	{
		group.setCreatedBy(currentUserId);
		return storage().save(group);
	}

	public Group update(final String id, final Group group, final String currentUserId)
	{
		final Group existing = findById(id);
		requireCreator(existing, currentUserId);

		existing.setName(group.getName());
		existing.setDescription(group.getDescription());

		return storage().save(existing);
	}

	public void delete(final String id, final String currentUserId)
	{
		final Group existing = findById(id);
		requireCreator(existing, currentUserId);

		storage().deleteById(id);
	}

	private void requireCreator(final Group group, final String currentUserId)
	{
		if (!currentUserId.equals(group.getCreatedBy()))
		{
			throw new AccessDeniedException("Only the group creator can perform this action");
		}
	}

	private DataStorage<Group> storage()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return _storageResolver.resolve(storageType, Group.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
