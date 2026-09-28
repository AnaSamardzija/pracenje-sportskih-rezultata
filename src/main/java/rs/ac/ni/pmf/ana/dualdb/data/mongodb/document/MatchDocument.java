package rs.ac.ni.pmf.ana.dualdb.data.mongodb.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "matches")
@CompoundIndex(name = "ix_matches_players", def = "{ 'sides.playerIds': 1 }")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDocument
{
	@Id
	private String id;

	@Indexed(name = "ix_matches_sport")
	private String sportId;

	@Indexed(name = "ix_matches_group")
	private String groupId;

	private LocalDateTime playedAt;
	private String recordedBy;

	@Builder.Default
	private List<MatchSideDocument> sides = new ArrayList<>();
}
