package rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportRulesEmbeddable
{
	@Column(name = "allow_draw", nullable = false)
	private boolean allowDraw;

	@Column(name = "min_players_per_side", nullable = false)
	private int minPlayersPerSide;

	@Column(name = "max_players_per_side")
	private Integer maxPlayersPerSide;

	@Column(name = "best_of")
	private Integer bestOf;

	@Column(name = "points_to_win_set")
	private Integer pointsToWinSet;

	@Column(name = "points_for_win", nullable = false)
	private int pointsForWin;

	@Column(name = "points_for_draw", nullable = false)
	private int pointsForDraw;

	@Column(name = "points_for_loss", nullable = false)
	private int pointsForLoss;
}
