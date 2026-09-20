package rs.ac.ni.pmf.marko.dualdb.dto.match;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.marko.dualdb.model.MatchOutcome;

import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MatchSideRequest
{
	@NotEmpty(message = "playerIds are required")
	List<String> playerIds;

	Integer score;

	List<Integer> setScores;

	MatchOutcome outcome;
}
