package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

/**
 * Jedan red rang-liste: mesto na listi + statistika igrača.
 * Nije entitet — sklapa se u servisu.
 */
@Data
@Builder
public class RankingEntry
{
	int rank;
	PlayerStats stats;
}
