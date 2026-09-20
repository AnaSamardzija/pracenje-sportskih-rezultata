package rs.ac.ni.pmf.marko.dualdb.dto.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PlayerStatsResponse
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
