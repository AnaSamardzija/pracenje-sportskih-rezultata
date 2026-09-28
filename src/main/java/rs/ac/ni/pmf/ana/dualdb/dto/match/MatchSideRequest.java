package rs.ac.ni.pmf.ana.dualdb.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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
public class MatchSideRequest
{
	@NotEmpty(message = "playerIds are required")
	@Schema(example = "[\"2\"]")
	List<String> playerIds;

	Integer score;

	@Schema(example = "[6, 4, 6]")
	List<Integer> setScores;

	MatchOutcome outcome;
}
