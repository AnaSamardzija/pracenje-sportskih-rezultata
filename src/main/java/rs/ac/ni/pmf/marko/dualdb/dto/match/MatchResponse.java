package rs.ac.ni.pmf.marko.dualdb.dto.match;

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
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MatchResponse
{
	String id;
	SportSummaryDto sport;
	GroupSummaryDto group;
	LocalDateTime playedAt;
	PlayerDto recordedBy;
	List<MatchSideResponse> sides;
}
