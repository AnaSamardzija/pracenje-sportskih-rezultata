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
public class RankingEntryResponse
{
	@Schema(example = "1")
	int rank;
	@Schema(example = "2")
	String playerId;
	@Schema(example = "pera")
	String username;
	@Schema(example = "3")
	int wins;
	@Schema(example = "0")
	int draws;
	@Schema(example = "0")
	int losses;
	@Schema(example = "3")
	int total;
	@Schema(example = "9")
	int points;
}
