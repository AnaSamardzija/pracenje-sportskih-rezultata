package rs.ac.ni.pmf.ana.dualdb.data.mongodb.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import rs.ac.ni.pmf.ana.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.ana.dualdb.model.SportType;

@Document(collection = "sports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportDocument
{
	@Id
	private String id;

	@Indexed(name = "uk_sports_name", unique = true, collation = "{ 'locale': 'en', 'strength': 1 }")
	private String name;

	private SportType type;
	private ScoringMode scoringMode;

	private SportRulesDocument rules;

	private boolean active;
}
