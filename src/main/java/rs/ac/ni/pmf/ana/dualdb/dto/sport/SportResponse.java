package rs.ac.ni.pmf.ana.dualdb.dto.sport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.ana.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.ana.dualdb.model.SportType;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SportResponse
{
	@Schema(example = "1")
	String id;
	@Schema(example = "Tennis")
	String name;
	@Schema(example = "INDIVIDUAL")
	SportType type;
	@Schema(example = "SETS")
	ScoringMode scoringMode;
	SportRulesDto rules;
	@Schema(example = "true")
	boolean active;
}
