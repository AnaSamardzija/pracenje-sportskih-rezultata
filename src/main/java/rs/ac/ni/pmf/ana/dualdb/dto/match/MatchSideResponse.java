package rs.ac.ni.pmf.ana.dualdb.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.ana.dualdb.model.MatchOutcome;

import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MatchSideResponse
{
	List<PlayerDto> players;
	@Schema(example = "2")
	Integer score;
	@Schema(example = "[6, 4, 6]")
	List<Integer> setScores;
	@Schema(example = "WIN")
	MatchOutcome outcome;
	@Schema(example = "true")
	boolean winner;
}
