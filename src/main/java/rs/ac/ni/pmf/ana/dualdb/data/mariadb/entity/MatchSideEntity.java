package rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import rs.ac.ni.pmf.ana.dualdb.model.MatchOutcome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "match_sides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSideEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id", nullable = false)
	private MatchEntity match;

	@Column(name = "score")
	private Integer score;

	@Enumerated(EnumType.STRING)
	@Column(name = "outcome", length = 20)
	private MatchOutcome outcome;

	@Column(name = "is_winner", nullable = false)
	private boolean winner;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "match_side_players",
			joinColumns = @JoinColumn(name = "match_side_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	@BatchSize(size = 50)
	@Builder.Default
	private Set<UserEntity> players = new HashSet<>();

	@ElementCollection
	@CollectionTable(
			name = "match_side_set_scores",
			joinColumns = @JoinColumn(name = "match_side_id")
	)
	@OrderColumn(name = "set_index")
	@Column(name = "score")
	@BatchSize(size = 50)
	@Builder.Default
	private List<Integer> setScores = new ArrayList<>();
}
