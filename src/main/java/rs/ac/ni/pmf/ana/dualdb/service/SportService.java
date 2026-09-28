package rs.ac.ni.pmf.ana.dualdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;
import rs.ac.ni.pmf.ana.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.ana.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.ana.dualdb.storage.sport.SportStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
		final Sport saved = storage().save(sport);

		log.info("Sport {} '{}' created", saved.getId(), saved.getName());
		return saved;
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
		final Sport saved = storage().save(existing);

		log.info("Sport {} '{}' updated", saved.getId(), saved.getName());
		return saved;
	}

	public void delete(final String id)
	{
		final Sport existing = findById(id, false);
		existing.setActive(false);
		storage().save(existing);

		log.info("Sport {} '{}' deactivated", existing.getId(), existing.getName());
	}

	public Sport restore(final String id)
	{
		final Sport existing = findById(id, true);

		if (existing.isActive())
		{
			throw new InvalidOperationException("Sport with id " + id + " is already active");
		}

		existing.setActive(true);
		final Sport saved = storage().save(existing);

		log.info("Sport {} '{}' restored", saved.getId(), saved.getName());
		return saved;
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
