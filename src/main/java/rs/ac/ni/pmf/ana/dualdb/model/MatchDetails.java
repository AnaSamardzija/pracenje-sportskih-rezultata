package rs.ac.ni.pmf.ana.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Izvedeni prikaz meča: meč + imena sporta, grupe i igrača.
 * Nije entitet — sklapa se u servisu.
 */
@Data
@Builder
public class MatchDetails
{
	Match match;
	String sportName;
	String groupName;
	String recordedByUsername;
	Map<String, String> playerUsernames;
}
