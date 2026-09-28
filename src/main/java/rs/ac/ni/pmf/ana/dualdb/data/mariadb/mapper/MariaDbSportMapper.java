package rs.ac.ni.pmf.ana.dualdb.data.mariadb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.SportEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.SportRulesEmbeddable;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;

@Component
public class MariaDbSportMapper
{
	public Sport toModel(final SportEntity entity)
	{
		return Sport.builder()
				.id(String.valueOf(entity.getId()))
				.name(entity.getName())
				.type(entity.getType())
				.scoringMode(entity.getScoringMode())
				.rules(toRules(entity.getRules()))
				.active(entity.isActive())
				.build();
	}

	public SportEntity toEntity(final Sport sport)
	{
		return SportEntity.builder()
				.id(sport.getId() == null ? null : Long.parseLong(sport.getId()))
				.name(sport.getName())
				.type(sport.getType())
				.scoringMode(sport.getScoringMode())
				.rules(toEmbeddable(sport.getRules()))
				.active(sport.isActive())
				.build();
	}

	private SportRules toRules(final SportRulesEmbeddable embeddable)
	{
		if (embeddable == null)
		{
			return null;
		}

		return SportRules.builder()
				.allowDraw(embeddable.isAllowDraw())
				.minPlayersPerSide(embeddable.getMinPlayersPerSide())
				.maxPlayersPerSide(embeddable.getMaxPlayersPerSide())
				.bestOf(embeddable.getBestOf())
				.pointsToWinSet(embeddable.getPointsToWinSet())
				.pointsForWin(embeddable.getPointsForWin())
				.pointsForDraw(embeddable.getPointsForDraw())
				.pointsForLoss(embeddable.getPointsForLoss())
				.build();
	}

	private SportRulesEmbeddable toEmbeddable(final SportRules rules)
	{
		if (rules == null)
		{
			return null;
		}

		return SportRulesEmbeddable.builder()
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
