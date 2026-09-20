package rs.ac.ni.pmf.marko.dualdb.dto.match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MatchRequest
{
	@NotBlank(message = "sportId is required")
	String sportId;

	@NotBlank(message = "groupId is required")
	String groupId;

	@NotNull(message = "playedAt is required")
	@PastOrPresent(message = "playedAt cannot be in the future")
	LocalDateTime playedAt;

	@NotNull(message = "sides are required")
	@Size(min = 2, max = 2, message = "a match must have exactly 2 sides")
	@Valid
	List<MatchSideRequest> sides;
}
