package rs.ac.ni.pmf.ana.dualdb.data.mongodb.document;

import lombok.*;
import rs.ac.ni.pmf.ana.dualdb.model.MatchOutcome;

import java.util.ArrayList;
import java.util.List;

/**
 * Strana meča kao ugnežđen pod-dokument — u MariaDB su to tri tabele (match_sides,
 * match_side_players, match_side_set_scores).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSideDocument
{
	@Builder.Default
	private List<String> playerIds = new ArrayList<>();

	private Integer score;

	@Builder.Default
	private List<Integer> setScores = new ArrayList<>();

	private MatchOutcome outcome;
	private boolean winner;
}
