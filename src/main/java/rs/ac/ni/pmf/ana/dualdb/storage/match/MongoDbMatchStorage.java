package rs.ac.ni.pmf.ana.dualdb.storage.match;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.MatchDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.MatchSideDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper.MongoMatchMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoMatchRepository;
import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.model.MatchOutcome;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class MongoDbMatchStorage extends MatchStorage
{
	private final MongoMatchRepository _matchRepository;
	private final MongoTemplate _mongoTemplate;
	private final MongoMatchMapper _matchMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MONGODB;
	}

	@Override
	public List<Match> findAll()
	{
		return _matchRepository.findAll().stream()
				.map(_matchMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	public List<Match> findAll(final String groupId, final String sportId, final String playerId)
	{
		final Query query = new Query();

		if (isPresent(groupId))
		{
			query.addCriteria(Criteria.where("groupId").is(groupId));
		}

		if (isPresent(sportId))
		{
			query.addCriteria(Criteria.where("sportId").is(sportId));
		}

		if (isPresent(playerId))
		{
			query.addCriteria(Criteria.where("sides.playerIds").is(playerId));
		}

		query.with(Sort.by(Sort.Direction.DESC, "playedAt", "id"));

		return _mongoTemplate.find(query, MatchDocument.class).stream()
				.map(_matchMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	public boolean existsByGroupId(final String groupId)
	{
		return _matchRepository.existsByGroupId(groupId);
	}

	@Override
	public int longestWinStreak(final String playerId, final String sportId)
	{
		final Query query = new Query(Criteria.where("sides.playerIds").is(playerId));

		if (isPresent(sportId))
		{
			query.addCriteria(Criteria.where("sportId").is(sportId));
		}

		query.with(Sort.by(Sort.Direction.ASC, "playedAt", "id"));

		int longest = 0;
		int current = 0;

		try (final Stream<MatchDocument> matches = _mongoTemplate.stream(query, MatchDocument.class))
		{
			final Iterator<MatchDocument> cursor = matches.iterator();

			while (cursor.hasNext())
			{
				if (outcomeOf(cursor.next(), playerId) == MatchOutcome.WIN)
				{
					current++;
					longest = Math.max(longest, current);
				}
				else
				{
					current = 0;
				}
			}
		}

		return longest;
	}

	@Override
	public Optional<Match> findById(final String id)
	{
		return _matchRepository.findById(id).map(_matchMapper::toModel);
	}

	@Override
	public Match save(final Match match)
	{
		final MatchDocument saved = _matchRepository.save(_matchMapper.toDocument(match));
		return _matchMapper.toModel(saved);
	}

	@Override
	public void deleteById(final String id)
	{
		_matchRepository.deleteById(id);
	}

	private MatchOutcome outcomeOf(final MatchDocument match, final String playerId)
	{
		return match.getSides().stream()
				.filter(side -> side.getPlayerIds().contains(playerId))
				.findFirst()
				.map(MatchSideDocument::getOutcome)
				.orElse(null);
	}

	private boolean isPresent(final String id)
	{
		return id != null && !id.isBlank();
	}
}
