package rs.ac.ni.pmf.marko.dualdb.data.mariadb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.MatchEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.MatchSideEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.model.Match;
import rs.ac.ni.pmf.marko.dualdb.model.MatchSide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MariaDbMatchMapper
{
	public Match toModel(final MatchEntity entity)
	{
		return Match.builder()
				.id(String.valueOf(entity.getId()))
				.sportId(entity.getSport() == null ? null : String.valueOf(entity.getSport().getId()))
				.groupId(entity.getGroup() == null ? null : String.valueOf(entity.getGroup().getId()))
				.playedAt(entity.getPlayedAt())
				.recordedBy(entity.getRecordedBy() == null ? null : String.valueOf(entity.getRecordedBy().getId()))
				.sides(entity.getSides().stream()
						.map(this::toSide)
						.collect(Collectors.toList()))
				.build();
	}

	public MatchSideEntity toSideEntity(final MatchSide side, final Set<UserEntity> players)
	{
		return MatchSideEntity.builder()
				.score(side.getScore())
				.outcome(side.getOutcome())
				.winner(side.isWinner())
				.players(players)
				.setScores(new ArrayList<>(side.getSetScores()))
				.build();
	}

	private MatchSide toSide(final MatchSideEntity entity)
	{
		final List<String> playerIds = entity.getPlayers().stream()
				.map(UserEntity::getId)
				.sorted()
				.map(String::valueOf)
				.collect(Collectors.toList());

		return MatchSide.builder()
				.playerIds(playerIds)
				.score(entity.getScore())
				.setScores(new ArrayList<>(entity.getSetScores()))
				.outcome(entity.getOutcome())
				.winner(entity.isWinner())
				.build();
	}
}
