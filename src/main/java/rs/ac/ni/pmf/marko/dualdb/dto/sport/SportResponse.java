package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.marko.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.marko.dualdb.model.SportType;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class SportResponse
{
	String id;
	String name;
	SportType type;
	ScoringMode scoringMode;
	SportRulesDto rules;
	boolean active;
}
