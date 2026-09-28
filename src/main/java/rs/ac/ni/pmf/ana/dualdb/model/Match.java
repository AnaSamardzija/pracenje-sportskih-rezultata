package rs.ac.ni.pmf.ana.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Match implements DataModel
{
	String id;

	String sportId;
	String groupId;

	LocalDateTime playedAt;
	String recordedBy;

	@Builder.Default
	List<MatchSide> sides = new ArrayList<>();
}
