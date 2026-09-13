package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;

	public List<Sport> findAll()
	{
		return storage().findAll();
	}

	public Sport findById(final String id)
	{
		return storage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Sport sa id " + id + " ne postoji"));
	}

	public Sport create(final Sport sport)
	{
		sport.setActive(true);
		return storage().save(sport);
	}

	public Sport update(final String id, final Sport sport)
	{
		final Sport existing = findById(id);
		existing.setName(sport.getName());
		existing.setType(sport.getType());
		existing.setScoringMode(sport.getScoringMode());
		existing.setRules(sport.getRules());

		return storage().save(existing);
	}

	public void delete(final String id)
	{
		final Sport existing = findById(id);
		existing.setActive(false);
		storage().save(existing);
	}

	private DataStorage<Sport> storage()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return _storageResolver.resolve(storageType, Sport.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
