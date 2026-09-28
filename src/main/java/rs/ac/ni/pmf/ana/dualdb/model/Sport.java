package rs.ac.ni.pmf.ana.dualdb.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Sport implements DataModel
{
	String id;

	String name;
	SportType type;
	ScoringMode scoringMode;

	SportRules rules;

	boolean active;
}
