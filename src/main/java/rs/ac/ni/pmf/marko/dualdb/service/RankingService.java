package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Group;
import rs.ac.ni.pmf.marko.dualdb.model.Match;
import rs.ac.ni.pmf.marko.dualdb.model.PlayerStats;
import rs.ac.ni.pmf.marko.dualdb.model.RankingEntry;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.model.User;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.marko.dualdb.storage.match.MatchStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.sport.SportStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;
	private final RankingCalculator _calculator;

	public List<RankingEntry> ranking(final String groupId, final String sportId)
	{
		requireGroupExists(groupId);
		requireSportExists(sportId);

		final List<PlayerStats> stats = withUsernames(
				_calculator.aggregate(matchStorage().findAll(groupId, sportId, null), sportsById()));

		stats.sort(Comparator.comparingInt(PlayerStats::getPoints).reversed()
				.thenComparing(Comparator.comparingInt(PlayerStats::getWins).reversed())
				.thenComparing(PlayerStats::getUsername, Comparator.nullsLast(Comparator.naturalOrder())));

		return withRanks(stats);
	}

	public PlayerStats playerStats(final String playerId, final String sportId)
	{
		final User player = userStorage().findById(playerId)
				.orElseThrow(() -> new ResourceNotFoundException("User with id " + playerId + " not found"));

		requireSportExists(sportId);

		final PlayerStats stats = _calculator.aggregate(matchStorage().findAll(null, sportId, playerId), sportsById())
				.stream()
				.filter(entry -> playerId.equals(entry.getPlayerId()))
				.findFirst()
				.orElseGet(() -> _calculator.empty(playerId));

		stats.setUsername(player.getUsername());

		return stats;
	}

	private List<PlayerStats> withUsernames(final List<PlayerStats> stats)
	{
		final Map<String, String> usernames = userStorage().findAll().stream()
				.collect(Collectors.toMap(User::getId, User::getUsername));

		stats.forEach(entry -> entry.setUsername(usernames.get(entry.getPlayerId())));

		return stats;
	}

	/**
	 * Izjednačeni igrači (isti bodovi i pobede) dele isto mesto, a sledeće se preskače: 1, 2, 2, 4.
	 */
	private List<RankingEntry> withRanks(final List<PlayerStats> sorted)
	{
		final List<RankingEntry> entries = new ArrayList<>();
		int rank = 0;
		PlayerStats previous = null;

		for (final PlayerStats stats : sorted)
		{
			if (previous == null || stats.getPoints() != previous.getPoints() || stats.getWins() != previous.getWins())
			{
				rank = entries.size() + 1;
			}

			entries.add(RankingEntry.builder().rank(rank).stats(stats).build());
			previous = stats;
		}

		return entries;
	}

	private Map<String, Sport> sportsById()
	{
		return sportStorage().findAll().stream()
				.collect(Collectors.toMap(Sport::getId, Function.identity()));
	}

	private void requireGroupExists(final String groupId)
	{
		if (groupId == null || groupId.isBlank())
		{
			return;
		}

		groupStorage().findById(groupId)
				.orElseThrow(() -> new ResourceNotFoundException("Group with id " + groupId + " not found"));
	}

	private void requireSportExists(final String sportId)
	{
		if (sportId == null || sportId.isBlank())
		{
			return;
		}

		sportStorage().findById(sportId)
				.orElseThrow(() -> new ResourceNotFoundException("Sport with id " + sportId + " not found"));
	}

	private MatchStorage matchStorage()
	{
		return (MatchStorage) resolve(Match.class);
	}

	private SportStorage sportStorage()
	{
		return (SportStorage) resolve(Sport.class);
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
