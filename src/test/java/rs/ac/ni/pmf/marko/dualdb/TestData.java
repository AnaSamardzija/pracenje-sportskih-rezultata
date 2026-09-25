package rs.ac.ni.pmf.marko.dualdb;

import rs.ac.ni.pmf.marko.dualdb.dto.sport.SportRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.sport.SportRulesDto;
import rs.ac.ni.pmf.marko.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.model.SportRules;
import rs.ac.ni.pmf.marko.dualdb.model.SportType;

public class TestData
{
	public static class SPORTS
	{
		public static final String TENIS_ID = "1";
		public static final String SAH_ID = "2";

		public static Sport newTenis()
		{
			return Sport.builder()
					.name("Tenis")
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

		public static Sport tenis()
		{
			final Sport sport = newTenis();
			sport.setId(TENIS_ID);
			sport.setActive(true);
			return sport;
		}

		public static SportRequest tenisRequest()
		{
			return SportRequest.builder()
					.name("Tenis")
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

		public static Sport newFudbal()
		{
			return Sport.builder()
					.name("Fudbal")
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

		public static Sport sah()
		{
			return Sport.builder()
					.id(SAH_ID)
					.name("Sah")
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
