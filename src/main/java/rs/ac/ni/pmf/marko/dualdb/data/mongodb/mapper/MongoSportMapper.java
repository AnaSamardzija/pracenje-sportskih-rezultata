package rs.ac.ni.pmf.marko.dualdb.data.mongodb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.SportDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.SportRulesDocument;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.model.SportRules;

@Component
public class MongoSportMapper
{
	public Sport toModel(final SportDocument document)
	{
		return Sport.builder()
				.id(document.getId())
				.name(document.getName())
				.type(document.getType())
				.scoringMode(document.getScoringMode())
				.rules(toRules(document.getRules()))
				.active(document.isActive())
				.build();
	}

	public SportDocument toDocument(final Sport sport)
	{
		return SportDocument.builder()
				.id(sport.getId())
				.name(sport.getName())
				.type(sport.getType())
				.scoringMode(sport.getScoringMode())
				.rules(toRulesDocument(sport.getRules()))
				.active(sport.isActive())
				.build();
	}

	private SportRules toRules(final SportRulesDocument document)
	{
		if (document == null)
		{
			return null;
		}

		return SportRules.builder()
				.allowDraw(document.isAllowDraw())
				.minPlayersPerSide(document.getMinPlayersPerSide())
				.maxPlayersPerSide(document.getMaxPlayersPerSide())
				.bestOf(document.getBestOf())
				.pointsToWinSet(document.getPointsToWinSet())
				.pointsForWin(document.getPointsForWin())
				.pointsForDraw(document.getPointsForDraw())
				.pointsForLoss(document.getPointsForLoss())
				.build();
	}

	private SportRulesDocument toRulesDocument(final SportRules rules)
	{
		if (rules == null)
		{
			return null;
		}

		return SportRulesDocument.builder()
				.allowDraw(rules.isAllowDraw())
				.minPlayersPerSide(rules.getMinPlayersPerSide())
				.maxPlayersPerSide(rules.getMaxPlayersPerSide())
				.bestOf(rules.getBestOf())
				.pointsToWinSet(rules.getPointsToWinSet())
				.pointsForWin(rules.getPointsForWin())
				.pointsForDraw(rules.getPointsForDraw())
				.pointsForLoss(rules.getPointsForLoss())
				.build();
	}
}
