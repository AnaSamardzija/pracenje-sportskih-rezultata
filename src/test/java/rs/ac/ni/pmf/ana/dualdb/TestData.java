package rs.ac.ni.pmf.ana.dualdb;

import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportRulesDto;
import rs.ac.ni.pmf.ana.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;
import rs.ac.ni.pmf.ana.dualdb.model.SportType;

public class TestData
{
	public static class SPORTS
	{
		public static final String TENNIS_ID = "1";
		public static final String CHESS_ID = "3";

		public static Sport newTennis()
		{
			return Sport.builder()
					.name("Tennis")
					.type(SportType.INDIVIDUAL)
					.scoringMode(ScoringMode.SETS)
					.rules(SportRules.builder()
							       .allowDraw(false)
							       .minPlayersPerSide(1)
							       .maxPlayersPerSide(1)
							       .bestOf(3)
							       .pointsToWinSet(6)
							       .pointsForWin(3)
							       .pointsForDraw(1)
							       .pointsForLoss(0)
							       .build())
					.build();
		}

		public static Sport tennis()
		{
			final Sport sport = newTennis();
			sport.setId(TENNIS_ID);
			sport.setActive(true);
			return sport;
		}

		public static SportRequest tennisRequest()
		{
			return SportRequest.builder()
					.name("Tennis")
					.type(SportType.INDIVIDUAL)
					.scoringMode(ScoringMode.SETS)
					.rules(SportRulesDto.builder()
							       .allowDraw(false)
							       .minPlayersPerSide(1)
							       .maxPlayersPerSide(1)
							       .bestOf(3)
							       .pointsToWinSet(6)
							       .pointsForWin(3)
							       .pointsForDraw(1)
							       .pointsForLoss(0)
							       .build())
					.build();
		}

		public static Sport newFootball()
		{
			return Sport.builder()
					.name("Football")
					.type(SportType.TEAM)
					.scoringMode(ScoringMode.POINTS)
					.rules(SportRules.builder()
							       .allowDraw(true)
							       .minPlayersPerSide(1)
							       .maxPlayersPerSide(11)
							       .pointsForWin(3)
							       .pointsForDraw(1)
							       .pointsForLoss(0)
							       .build())
					.build();
		}

		public static Sport chess()
		{
			return Sport.builder()
					.id(CHESS_ID)
					.name("Chess")
					.type(SportType.INDIVIDUAL)
					.scoringMode(ScoringMode.OUTCOME)
					.rules(SportRules.builder()
							       .allowDraw(true)
							       .minPlayersPerSide(1)
							       .maxPlayersPerSide(1)
							       .pointsForWin(2)
							       .pointsForDraw(1)
							       .pointsForLoss(0)
							       .build())
					.active(true)
					.build();
		}
	}
}
