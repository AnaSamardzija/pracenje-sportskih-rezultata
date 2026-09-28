package rs.ac.ni.pmf.ana.dualdb.data.mongodb.document;

import lombok.*;

/**
 * Pravila sporta kao pod-dokument unutar SportDocument-a (nema svoju kolekciju),
 * Mongo ekvivalent SportRulesEmbeddable-a.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportRulesDocument
{
	private boolean allowDraw;
	private int minPlayersPerSide;
	private Integer maxPlayersPerSide;
	private Integer bestOf;
	private Integer pointsToWinSet;
	private int pointsForWin;
	private int pointsForDraw;
	private int pointsForLoss;
}
