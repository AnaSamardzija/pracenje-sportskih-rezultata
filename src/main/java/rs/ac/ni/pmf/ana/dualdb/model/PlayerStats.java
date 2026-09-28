package rs.ac.ni.pmf.ana.dualdb.model;

import lombok.Builder;
import lombok.Data;

/**
 * Izvedena statistika jednog igrača (pobede/nerešeno/porazi, bodovi, procenat, najduži niz pobeda).
 * Nije entitet — računa se iz mečeva u RankingCalculator-u, a najduži niz pobeda u bazi (MatchStorage).
 */
@Data
@Builder
public class PlayerStats
{
	String playerId;
	String username;

	int wins;
	int draws;
	int losses;
	int total;
	int points;

	double winPercentage;
	int longestWinStreak;
}
