package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SportRulesDto
{
	@NotNull(message = "allowDraw is required")
	@Schema(example = "false")
	Boolean allowDraw;

	@NotNull(message = "minPlayersPerSide is required")
	@Min(value = 1, message = "minPlayersPerSide must be at least 1")
	@Schema(example = "1")
	Integer minPlayersPerSide;

	@Min(value = 1, message = "maxPlayersPerSide must be at least 1")
	@Schema(example = "1")
	Integer maxPlayersPerSide;

	@Min(value = 1, message = "bestOf must be at least 1")
	@Schema(example = "3")
	Integer bestOf;

	@Min(value = 1, message = "pointsToWinSet must be at least 1")
	@Schema(example = "6")
	Integer pointsToWinSet;

	@NotNull(message = "pointsForWin is required")
	@Min(value = 0, message = "pointsForWin cannot be negative")
	@Schema(example = "3")
	Integer pointsForWin;

	@NotNull(message = "pointsForDraw is required")
	@Min(value = 0, message = "pointsForDraw cannot be negative")
	@Schema(example = "1")
	Integer pointsForDraw;

	@NotNull(message = "pointsForLoss is required")
	@Min(value = 0, message = "pointsForLoss cannot be negative")
	@Schema(example = "0")
	Integer pointsForLoss;
}
