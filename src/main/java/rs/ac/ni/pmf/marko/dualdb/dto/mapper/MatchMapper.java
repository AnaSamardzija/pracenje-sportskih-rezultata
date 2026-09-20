package rs.ac.ni.pmf.marko.dualdb.dto.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.dto.group.GroupSummaryDto;
import rs.ac.ni.pmf.marko.dualdb.dto.match.MatchRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.match.MatchResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.match.MatchSideRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.match.MatchSideResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.match.PlayerDto;
import rs.ac.ni.pmf.marko.dualdb.dto.sport.SportSummaryDto;
import rs.ac.ni.pmf.marko.dualdb.model.Match;
import rs.ac.ni.pmf.marko.dualdb.model.MatchDetails;
import rs.ac.ni.pmf.marko.dualdb.model.MatchSide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MatchMapper
{
	public MatchResponse toResponse(final MatchDetails details)
	{
		final Match match = details.getMatch();

		return MatchResponse.builder()
				.id(match.getId())
				.sport(SportSummaryDto.builder().id(match.getSportId()).name(details.getSportName()).build())
				.group(GroupSummaryDto.builder().id(match.getGroupId()).name(details.getGroupName()).build())
				.playedAt(match.getPlayedAt())
				.recordedBy(PlayerDto.builder()
						.id(match.getRecordedBy())
						.username(details.getRecordedByUsername())
						.build())
				.sides(match.getSides().stream()
						.map(side -> toSideResponse(side, details.getPlayerUsernames()))
						.collect(Collectors.toList()))
				.build();
	}

	public Match toModel(final MatchRequest request)
	{
		return Match.builder()
				.sportId(request.getSportId())
				.groupId(request.getGroupId())
				.playedAt(request.getPlayedAt())
				.sides(request.getSides().stream()
						.map(this::toSide)
						.collect(Collectors.toList()))
				.build();
	}

	private MatchSide toSide(final MatchSideRequest request)
	{
		return MatchSide.builder()
				.playerIds(request.getPlayerIds() == null ? new ArrayList<>() : new ArrayList<>(request.getPlayerIds()))
				.score(request.getScore())
				.setScores(request.getSetScores() == null ? new ArrayList<>() : new ArrayList<>(request.getSetScores()))
				.outcome(request.getOutcome())
				.build();
	}

	private MatchSideResponse toSideResponse(final MatchSide side, final Map<String, String> usernames)
	{
		final List<PlayerDto> players = side.getPlayerIds().stream()
				.map(playerId -> PlayerDto.builder()
						.id(playerId)
						.username(usernames.get(playerId))
						.build())
				.collect(Collectors.toList());

		return MatchSideResponse.builder()
				.players(players)
				.score(side.getScore())
				.setScores(side.getSetScores())
				.outcome(side.getOutcome())
				.winner(side.isWinner())
				.build();
	}
}
