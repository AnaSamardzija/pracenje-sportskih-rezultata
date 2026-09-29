package rs.ac.ni.pmf.ana.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Group;
import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.model.PlayerStats;
import rs.ac.ni.pmf.ana.dualdb.model.RankingEntry;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.User;
import rs.ac.ni.pmf.ana.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.ana.dualdb.storage.match.MatchStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.sport.SportStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

		final List<Match> matches = matchStorage().findAll(groupId, sportId, null);
		final List<PlayerStats> stats = withUsernames(_calculator.aggregate(matches, sportsById(matches)));

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

		final List<Match> matches = matchStorage().findAll(null, sportId, playerId);
		final PlayerStats stats = _calculator.aggregate(matches, sportsById(matches))
				.stream()
				.filter(entry -> playerId.equals(entry.getPlayerId()))
				.findFirst()
				.orElseGet(() -> _calculator.empty(playerId));

		stats.setUsername(player.getUsername());
		stats.setLongestWinStreak(matchStorage().longestWinStreak(playerId, sportId));

		return stats;
	}

	private List<PlayerStats> withUsernames(final List<PlayerStats> stats)
	{
		final Set<String> playerIds = stats.stream().map(PlayerStats::getPlayerId).collect(Collectors.toSet());
		final Map<String, String> usernames = userStorage().findAllById(playerIds).stream()
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

	private Map<String, Sport> sportsById(final List<Match> matches)
	{
		final Set<String> sportIds = matches.stream().map(Match::getSportId).collect(Collectors.toSet());

		return sportStorage().findAllById(sportIds).stream()
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
