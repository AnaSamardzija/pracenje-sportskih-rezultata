package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.marko.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.model.SportRules;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.marko.dualdb.storage.sport.SportStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;

	public List<Sport> findAll(final boolean includeInactive)
	{
		return includeInactive ? storage().findAll() : storage().findAllActive();
	}

	public Sport findById(final String id, final boolean includeInactive)
	{
		return storage().findById(id)
				.filter(sport -> includeInactive || sport.isActive())
				.orElseThrow(() -> new ResourceNotFoundException("Sport with id " + id + " not found"));
	}

	public Sport create(final Sport sport)
	{
		requireUniqueName(sport.getName(), null);
		requireValidPlayerRange(sport.getRules());

		sport.setActive(true);
		return storage().save(sport);
	}

	public Sport update(final String id, final Sport sport)
	{
		final Sport existing = findById(id, false);

		requireUniqueName(sport.getName(), id);
		requireValidPlayerRange(sport.getRules());

		existing.setName(sport.getName());
		existing.setType(sport.getType());
		existing.setScoringMode(sport.getScoringMode());
		existing.setRules(sport.getRules());

		return storage().save(existing);
	}

	public void delete(final String id)
	{
		final Sport existing = findById(id, false);
		existing.setActive(false);
		storage().save(existing);
	}

	public Sport restore(final String id)
	{
		final Sport existing = findById(id, true);

		if (existing.isActive())
		{
			throw new InvalidOperationException("Sport with id " + id + " is already active");
		}

		existing.setActive(true);
		return storage().save(existing);
	}

	/**
	 * Ime sporta je jedinstveno i među obrisanim (neaktivnim) sportovima, pa korisnik dobija jasnu
	 * poruku umesto greške iz baze — a za neaktivan sport i uputstvo da ga vrati.
	 */
	private void requireUniqueName(final String name, final String currentId)
	{
		storage().findByName(name)
				.filter(other -> !other.getId().equals(currentId))
				.ifPresent(other ->
				{
					throw new DuplicateResourceException(other.isActive()
							? "Sport with name " + name + " already exists"
							: "Sport with name " + name + " already exists but is inactive; restore it instead");
				});
	}

	private void requireValidPlayerRange(final SportRules rules)
	{
		if (rules.getMaxPlayersPerSide() != null && rules.getMaxPlayersPerSide() < rules.getMinPlayersPerSide())
		{
			throw new InvalidOperationException("maxPlayersPerSide cannot be less than minPlayersPerSide");
		}
	}

	private SportStorage storage()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return (SportStorage) _storageResolver.resolve(storageType, Sport.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
