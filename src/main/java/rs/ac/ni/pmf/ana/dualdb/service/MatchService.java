package rs.ac.ni.pmf.ana.dualdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Group;
import rs.ac.ni.pmf.ana.dualdb.model.GroupRole;
import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.model.MatchDetails;
import rs.ac.ni.pmf.ana.dualdb.model.MatchSide;
import rs.ac.ni.pmf.ana.dualdb.model.Permission;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;
import rs.ac.ni.pmf.ana.dualdb.model.User;
import rs.ac.ni.pmf.ana.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.ana.dualdb.storage.match.MatchStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.sport.SportStorage;
import rs.ac.ni.pmf.ana.dualdb.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;
	private final MembershipService _membershipService;
	private final MatchResultResolver _resultResolver;

	public List<MatchDetails> findAll(final String groupId, final String sportId, final String playerId)
	{
		if (groupId != null && !groupId.isBlank())
		{
			requireGroupExists(groupId);
		}

		if (sportId != null && !sportId.isBlank())
		{
			requireSportExists(sportId);
		}

		if (playerId != null && !playerId.isBlank())
		{
			requirePlayerExists(playerId);
		}

		return toDetails(matchStorage().findAll(groupId, sportId, playerId));
	}

	public MatchDetails findById(final String id)
	{
		return toDetails(List.of(loadMatch(id))).get(0);
	}

	public boolean hasMatchesInGroup(final String groupId)
	{
		return matchStorage().existsByGroupId(groupId);
	}

	public boolean hasMatchesInSport(final String sportId)
	{
		return matchStorage().existsBySportId(sportId);
	}

	@Transactional
	public MatchDetails create(final Match match, final User currentUser)
	{
		final Sport sport = loadActiveSport(match.getSportId());
		requireGroupExists(match.getGroupId());

		final Set<String> memberIds = _membershipService.memberIds(match.getGroupId());

		if (!currentUser.hasPermission(Permission.MATCHES_CREATE_ANY) && !memberIds.contains(currentUser.getId()))
		{
			throw new AccessDeniedException("Only a member of the group can record a match");
		}

		validateSides(sport, match, memberIds);
		_resultResolver.applyResult(sport, match);

		match.setRecordedBy(currentUser.getId());
		final Match saved = matchStorage().save(match);

		log.info("Match {} created in group {}", saved.getId(), saved.getGroupId());
		return toDetails(List.of(saved)).get(0);
	}

	@Transactional
	public MatchDetails update(final String id, final Match match, final User currentUser)
	{
		final Match existing = loadMatch(id);
		requireGroupAdmin(existing, currentUser, Permission.MATCHES_UPDATE_ANY);

		if (!existing.getGroupId().equals(match.getGroupId()))
		{
			throw new InvalidOperationException("A match cannot be moved to another group");
		}

		final Sport sport = loadActiveSport(match.getSportId());

		validateSides(sport, match, _membershipService.memberIds(existing.getGroupId()));
		_resultResolver.applyResult(sport, match);

		existing.setSportId(match.getSportId());
		existing.setPlayedAt(match.getPlayedAt());
		existing.setSides(match.getSides());
		final Match saved = matchStorage().save(existing);

		log.info("Match {} updated", saved.getId());
		return toDetails(List.of(saved)).get(0);
	}

	@Transactional
	public void delete(final String id, final User currentUser)
	{
		final Match existing = loadMatch(id);
		requireGroupAdmin(existing, currentUser, Permission.MATCHES_DELETE_ANY);

		matchStorage().deleteById(id);

		log.info("Match {} deleted", id);
	}

	private void requireGroupAdmin(final Match match, final User currentUser, final Permission anyGroupPermission)
	{
		if (!currentUser.hasPermission(anyGroupPermission)
				&& _membershipService.roleOf(match.getGroupId(), currentUser.getId()) != GroupRole.GROUP_ADMIN)
		{
			throw new AccessDeniedException("Only a group admin can edit or delete a match");
		}
	}

	private void validateSides(final Sport sport, final Match match, final Set<String> memberIds)
	{
		if (match.getSides().size() != 2)
		{
			throw new InvalidOperationException("A match must have exactly 2 sides");
		}

		final SportRules rules = sport.getRules();
		final Set<String> seenPlayers = new HashSet<>();

		for (final MatchSide side : match.getSides())
		{
			final List<String> playerIds = side.getPlayerIds();

			if (playerIds.isEmpty())
			{
				throw new InvalidOperationException("Every side must have at least one player");
			}

			if (playerIds.size() < rules.getMinPlayersPerSide())
			{
				throw new InvalidOperationException(
						"Sport " + sport.getName() + " requires at least " + rules.getMinPlayersPerSide() + " players per side");
			}

			if (rules.getMaxPlayersPerSide() != null && playerIds.size() > rules.getMaxPlayersPerSide())
			{
				throw new InvalidOperationException(
						"Sport " + sport.getName() + " allows at most " + rules.getMaxPlayersPerSide() + " players per side");
			}

			for (final String playerId : playerIds)
			{
				if (!seenPlayers.add(playerId))
				{
					throw new InvalidOperationException("Player with id " + playerId + " cannot appear more than once in a match");
				}

				if (!memberIds.contains(playerId))
				{
					throw new InvalidOperationException("Player with id " + playerId + " is not a member of this group");
				}

				if (!userStorage().findById(playerId).map(User::isActive).orElse(false))
				{
					throw new InvalidOperationException("Player with id " + playerId + " is deactivated");
				}
			}
		}
	}

	private Match loadMatch(final String id)
	{
		return matchStorage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Match with id " + id + " not found"));
	}

	private Sport loadActiveSport(final String id)
	{
		final Sport sport = sportStorage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Sport with id " + id + " not found"));

		if (!sport.isActive())
		{
			throw new InvalidOperationException("Sport with id " + id + " is no longer active");
		}

		return sport;
	}

	private void requireSportExists(final String id)
	{
		sportStorage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Sport with id " + id + " not found"));
	}

	private void requirePlayerExists(final String id)
	{
		userStorage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
	}

	private void requireGroupExists(final String id)
	{
		groupStorage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Group with id " + id + " not found"));
	}

	private List<MatchDetails> toDetails(final List<Match> matches)
	{
		final Map<String, String> sportNames = sportStorage().findAll().stream()
				.collect(Collectors.toMap(Sport::getId, Sport::getName));

		final Map<String, String> groupNames = groupStorage().findAll().stream()
				.collect(Collectors.toMap(Group::getId, Group::getName));

		final Map<String, String> usernames = userStorage().findAll().stream()
				.collect(Collectors.toMap(User::getId, User::getUsername));

		return matches.stream()
				.map(match -> MatchDetails.builder()
						.match(match)
						.sportName(sportNames.get(match.getSportId()))
						.groupName(groupNames.get(match.getGroupId()))
						.recordedByUsername(usernames.get(match.getRecordedBy()))
						.playerUsernames(playerUsernames(match, usernames))
						.build())
				.toList();
	}

	private Map<String, String> playerUsernames(final Match match, final Map<String, String> usernames)
	{
		return match.getSides().stream()
				.flatMap(side -> side.getPlayerIds().stream())
				.distinct()
				.filter(usernames::containsKey)
				.collect(Collectors.toMap(Function.identity(), usernames::get));
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
