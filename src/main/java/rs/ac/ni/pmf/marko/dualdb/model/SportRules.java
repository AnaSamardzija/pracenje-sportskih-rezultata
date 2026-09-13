package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SportRules
{
	boolean allowDraw;
	int minPlayersPerSide;
	Integer maxPlayersPerSide;
	Integer bestOf;
	Integer pointsToWinSet;
	int pointsForWin;
	int pointsForDraw;
	int pointsForLoss;
}
