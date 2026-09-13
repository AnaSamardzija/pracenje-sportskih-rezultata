package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SportRequest
{
	@NotBlank(message = "name is required")
	String name;

	@NotNull(message = "type is required (INDIVIDUAL or TEAM)")
	SportType type;

	@NotNull(message = "scoringMode is required (POINTS, SETS or OUTCOME)")
	ScoringMode scoringMode;

	@NotNull(message = "rules are required")
	@Valid
	SportRulesDto rules;
}
