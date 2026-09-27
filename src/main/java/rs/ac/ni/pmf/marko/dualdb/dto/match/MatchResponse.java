package rs.ac.ni.pmf.marko.dualdb.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.marko.dualdb.dto.group.GroupSummaryDto;
import rs.ac.ni.pmf.marko.dualdb.dto.sport.SportSummaryDto;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MatchResponse
{
	@Schema(example = "1")
	String id;
	SportSummaryDto sport;
	GroupSummaryDto group;
	@Schema(example = "2026-09-20T12:00:00")
	LocalDateTime playedAt;
	PlayerDto recordedBy;
	List<MatchSideResponse> sides;
}
