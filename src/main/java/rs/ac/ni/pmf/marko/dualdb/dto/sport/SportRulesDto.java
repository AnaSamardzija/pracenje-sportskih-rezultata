package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class SportRulesDto
{
	@NotNull(message = "allowDraw is required")
	Boolean allowDraw;

	@NotNull(message = "minPlayersPerSide is required")
	@Min(value = 1, message = "minPlayersPerSide must be at least 1")
	Integer minPlayersPerSide;

	@Min(value = 1, message = "maxPlayersPerSide must be at least 1")
	Integer maxPlayersPerSide;

	@Min(value = 1, message = "bestOf must be at least 1")
	Integer bestOf;

	@Min(value = 1, message = "pointsToWinSet must be at least 1")
	Integer pointsToWinSet;

	@NotNull(message = "pointsForWin is required")
	@Min(value = 0, message = "pointsForWin cannot be negative")
	Integer pointsForWin;

	@NotNull(message = "pointsForDraw is required")
	@Min(value = 0, message = "pointsForDraw cannot be negative")
	Integer pointsForDraw;

	@NotNull(message = "pointsForLoss is required")
	@Min(value = 0, message = "pointsForLoss cannot be negative")
	Integer pointsForLoss;
}
