package rs.ac.ni.pmf.ana.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Strana meča. Ne postoji samostalno — uvek je deo meča.
 * Polja score, outcome i winner postavlja sistem u MatchResultResolver-u.
 */
@Data
@Builder
public class MatchSide
{
	@Builder.Default
	List<String> playerIds = new ArrayList<>();

	Integer score;

	@Builder.Default
	List<Integer> setScores = new ArrayList<>();

	MatchOutcome outcome;
	boolean winner;
}
