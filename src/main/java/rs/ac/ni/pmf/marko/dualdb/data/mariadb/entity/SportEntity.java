package rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity;

import jakarta.persistence.*;
import lombok.*;
import rs.ac.ni.pmf.marko.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.marko.dualdb.model.SportType;

@Entity
@Table(name = "sports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SportType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "scoring_mode", nullable = false, length = 20)
	private ScoringMode scoringMode;

	@Embedded
	private SportRulesEmbeddable rules;

	@Column(nullable = false)
	private boolean active;
}
