package rs.ac.ni.pmf.marko.dualdb.dto.match;

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
public class MatchSideResponse
{
	List<PlayerDto> players;
	Integer score;
	List<Integer> setScores;
	MatchOutcome outcome;
	boolean winner;
}
