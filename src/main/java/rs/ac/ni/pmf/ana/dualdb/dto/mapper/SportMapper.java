package rs.ac.ni.pmf.ana.dualdb.dto.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportResponse;
import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportRulesDto;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;

@Component
public class SportMapper
{
	public SportResponse toResponse(final Sport sport)
	{
		return SportResponse.builder()
				.id(sport.getId())
				.name(sport.getName())
				.type(sport.getType())
				.scoringMode(sport.getScoringMode())
				.rules(toRulesDto(sport.getRules()))
				.active(sport.isActive())
				.build();
	}

	public Sport toModel(final SportRequest request)
	{
		return Sport.builder()
				.name(request.getName())
				.type(request.getType())
				.scoringMode(request.getScoringMode())
				.rules(toRules(request.getRules()))
				.build();
	}

	private SportRulesDto toRulesDto(final SportRules rules)
	{
		if (rules == null)
		{
			return null;
		}

		return SportRulesDto.builder()
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

	private SportRules toRules(final SportRulesDto dto)
	{
		if (dto == null)
		{
			return null;
		}

		return SportRules.builder()
				.allowDraw(dto.getAllowDraw())
				.minPlayersPerSide(dto.getMinPlayersPerSide())
				.maxPlayersPerSide(dto.getMaxPlayersPerSide())
				.bestOf(dto.getBestOf())
				.pointsToWinSet(dto.getPointsToWinSet())
				.pointsForWin(dto.getPointsForWin())
				.pointsForDraw(dto.getPointsForDraw())
				.pointsForLoss(dto.getPointsForLoss())
				.build();
	}
}
