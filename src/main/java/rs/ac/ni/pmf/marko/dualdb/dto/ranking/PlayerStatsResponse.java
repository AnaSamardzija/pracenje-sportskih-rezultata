package rs.ac.ni.pmf.marko.dualdb.dto.ranking;

import io.swagger.v3.oas.annotations.media.Schema;
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
	@Schema(example = "2")
	String playerId;
	@Schema(example = "pera")
	String username;
	@Schema(example = "1")
	int wins;
	@Schema(example = "1")
	int draws;
	@Schema(example = "3")
	int losses;
	@Schema(example = "5")
	int total;
	@Schema(example = "4")
	int points;
	@Schema(example = "20.0")
	double winPercentage;
}
