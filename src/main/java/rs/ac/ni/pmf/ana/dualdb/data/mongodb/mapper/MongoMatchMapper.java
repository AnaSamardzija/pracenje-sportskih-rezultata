package rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.MatchDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.MatchSideDocument;
import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.model.MatchSide;

import java.util.ArrayList;

@Component
public class MongoMatchMapper
{
	public Match toModel(final MatchDocument document)
	{
		return Match.builder()
				.id(document.getId())
				.sportId(document.getSportId())
				.groupId(document.getGroupId())
				.playedAt(document.getPlayedAt())
				.recordedBy(document.getRecordedBy())
				.sides(document.getSides().stream()
						.map(this::toSide)
						.toList())
				.build();
	}

	public MatchDocument toDocument(final Match match)
	{
		return MatchDocument.builder()
				.id(match.getId())
				.sportId(match.getSportId())
				.groupId(match.getGroupId())
				.playedAt(match.getPlayedAt())
				.recordedBy(match.getRecordedBy())
				.sides(match.getSides().stream()
						.map(this::toSideDocument)
						.toList())
				.build();
	}

	private MatchSide toSide(final MatchSideDocument document)
	{
		return MatchSide.builder()
				.playerIds(document.getPlayerIds().stream()
						.sorted()
						.toList())
				.score(document.getScore())
				.setScores(new ArrayList<>(document.getSetScores()))
				.outcome(document.getOutcome())
				.winner(document.isWinner())
				.build();
	}

	private MatchSideDocument toSideDocument(final MatchSide side)
	{
		return MatchSideDocument.builder()
				.playerIds(new ArrayList<>(side.getPlayerIds()))
				.score(side.getScore())
				.setScores(new ArrayList<>(side.getSetScores()))
				.outcome(side.getOutcome())
				.winner(side.isWinner())
				.build();
	}
}
