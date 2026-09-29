package rs.ac.ni.pmf.ana.dualdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;
import rs.ac.ni.pmf.ana.dualdb.model.SportType;
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
		requireValidPlayerRange(sport);
		requireValidScoringRules(sport);

		sport.setActive(true);
		final Sport saved = storage().save(sport);

		log.info("Sport {} '{}' created", saved.getId(), saved.getName());
		return saved;
	}

	public Sport update(final String id, final Sport sport)
	{
		final Sport existing = findById(id, false);

		requireUniqueName(sport.getName(), id);
		requireValidPlayerRange(sport);
		requireValidScoringRules(sport);

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

	private void requireValidPlayerRange(final Sport sport)
	{
		final SportRules rules = sport.getRules();

		if (sport.getType() == SportType.INDIVIDUAL
				&& (rules.getMinPlayersPerSide() != 1 || rules.getMaxPlayersPerSide() == null || rules.getMaxPlayersPerSide() != 1))
		{
			throw new InvalidOperationException("An individual sport must have exactly 1 player per side");
		}

		if (rules.getMaxPlayersPerSide() != null && rules.getMaxPlayersPerSide() < rules.getMinPlayersPerSide())
		{
			throw new InvalidOperationException("maxPlayersPerSide cannot be less than minPlayersPerSide");
		}
	}

	/**
	 * SETS sport se igra na neparan broj setova (bestOf), pa uvek ima pobednika i ne može nerešeno. Ostali
	 * načini bodovanja nemaju setove, pa ni bestOf ni pointsToWinSet.
	 */
	private void requireValidScoringRules(final Sport sport)
	{
		final SportRules rules = sport.getRules();

		if (sport.getScoringMode() != ScoringMode.SETS)
		{
			if (rules.getBestOf() != null || rules.getPointsToWinSet() != null)
			{
				throw new InvalidOperationException("bestOf and pointsToWinSet are used only for SETS scoring");
			}

			return;
		}

		if (rules.getBestOf() == null || rules.getBestOf() % 2 == 0)
		{
			throw new InvalidOperationException("A SETS sport requires an odd bestOf");
		}

		if (rules.getPointsToWinSet() == null)
		{
			throw new InvalidOperationException("A SETS sport requires pointsToWinSet");
		}

		if (rules.isAllowDraw())
		{
			throw new InvalidOperationException("A SETS sport cannot allow a draw");
		}
	}

	private SportStorage storage()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return (SportStorage) _storageResolver.resolve(storageType, Sport.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
