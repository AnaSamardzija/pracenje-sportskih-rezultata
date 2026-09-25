package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import io.swagger.v3.oas.annotations.media.Schema;
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
	@Schema(example = "1")
	String id;
	@Schema(example = "Tenis")
	String name;
	@Schema(example = "INDIVIDUAL")
	SportType type;
	@Schema(example = "SETS")
	ScoringMode scoringMode;
	SportRulesDto rules;
	@Schema(example = "true")
	boolean active;
}
