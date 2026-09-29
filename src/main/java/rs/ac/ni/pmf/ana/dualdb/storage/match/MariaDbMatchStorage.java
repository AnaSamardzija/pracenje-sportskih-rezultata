package rs.ac.ni.pmf.ana.dualdb.storage.match;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.MatchEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.MatchSideEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.mapper.MariaDbMatchMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbGroupRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbMatchRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbSportRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.model.MatchSide;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MariaDbMatchStorage extends MatchStorage
{
	private final MariaDbMatchRepository _matchRepository;
	private final MariaDbSportRepository _sportRepository;
	private final MariaDbGroupRepository _groupRepository;
	private final MariaDbUserRepository _userRepository;
	private final MariaDbMatchMapper _matchMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MARIADB;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Match> findAll()
	{
		return _matchRepository.findAll().stream()
				.map(_matchMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Match> findAll(final String groupId, final String sportId, final String playerId)
	{
		return _matchRepository.search(toId(groupId), toId(sportId), toId(playerId)).stream()
				.map(_matchMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByGroupId(final String groupId)
	{
		return _matchRepository.existsByGroup_Id(Long.parseLong(groupId));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsBySportId(final String sportId)
	{
		return _matchRepository.existsBySport_Id(Long.parseLong(sportId));
	}

	@Override
	@Transactional(readOnly = true)
	public int longestWinStreak(final String playerId, final String sportId)
	{
		return _matchRepository.longestWinStreak(Long.parseLong(playerId), toId(sportId));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Match> findById(final String id)
	{
		return _matchRepository.findById(Long.parseLong(id)).map(_matchMapper::toModel);
	}

	@Override
	@Transactional
	public Match save(final Match match)
	{
		final MatchEntity entity = loadOrCreate(match.getId());

		entity.setSport(_sportRepository.getReferenceById(Long.parseLong(match.getSportId())));
		entity.setGroup(_groupRepository.getReferenceById(Long.parseLong(match.getGroupId())));
		entity.setPlayedAt(match.getPlayedAt());
		entity.setRecordedBy(_userRepository.getReferenceById(Long.parseLong(match.getRecordedBy())));

		entity.getSides().clear();
		_matchRepository.saveAndFlush(entity);

		for (final MatchSide side : match.getSides())
		{
			final MatchSideEntity sideEntity = _matchMapper.toSideEntity(side, players(side));
			sideEntity.setMatch(entity);
			entity.getSides().add(sideEntity);
		}

		return _matchMapper.toModel(_matchRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional
	public void deleteById(final String id)
	{
		_matchRepository.deleteById(Long.parseLong(id));
	}

	private Long toId(final String id)
	{
		return id == null || id.isBlank() ? null : Long.parseLong(id);
	}

	private MatchEntity loadOrCreate(final String id)
	{
		if (id == null)
		{
			return MatchEntity.builder().build();
		}

		return _matchRepository.findById(Long.parseLong(id))
				.orElseThrow(() -> new ResourceNotFoundException("Match with id " + id + " not found"));
	}

	private Set<UserEntity> players(final MatchSide side)
	{
		return side.getPlayerIds().stream()
				.map(playerId -> _userRepository.getReferenceById(Long.parseLong(playerId)))
				.collect(Collectors.toSet());
	}
}
