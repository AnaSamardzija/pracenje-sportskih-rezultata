package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.marko.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.marko.dualdb.model.SportType;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SportRequest
{
	@NotBlank(message = "name is required")
	@Schema(example = "Tenis")
	String name;

	@NotNull(message = "type is required (INDIVIDUAL or TEAM)")
	@Schema(example = "INDIVIDUAL")
	SportType type;

	@NotNull(message = "scoringMode is required (POINTS, SETS or OUTCOME)")
	@Schema(example = "SETS")
	ScoringMode scoringMode;

	@NotNull(message = "rules are required")
	@Valid
	SportRulesDto rules;
}
