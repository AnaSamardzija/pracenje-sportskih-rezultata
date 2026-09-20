package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

/**
 * Izvedena statistika jednog igrača (pobede/nerešeno/porazi, bodovi, procenat).
 * Nije entitet — računa se iz mečeva u RankingCalculator-u.
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
}
