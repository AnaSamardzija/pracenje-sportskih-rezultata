package rs.ac.ni.pmf.ana.dualdb.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MatchRequest
{
	@NotBlank(message = "sportId is required")
	@Schema(example = "1")
	String sportId;

	@NotBlank(message = "groupId is required")
	@Schema(example = "1")
	String groupId;

	@NotNull(message = "playedAt is required")
	@PastOrPresent(message = "playedAt cannot be in the future")
	@Schema(example = "2026-09-20T12:00:00")
	LocalDateTime playedAt;

	@NotNull(message = "sides are required")
	@Size(min = 2, max = 2, message = "a match must have exactly 2 sides")
	List<@NotNull(message = "side cannot be null") @Valid MatchSideRequest> sides;
}
