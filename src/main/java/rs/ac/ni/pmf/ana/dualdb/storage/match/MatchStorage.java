package rs.ac.ni.pmf.ana.dualdb.storage.match;

import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;

import java.util.List;

public abstract class MatchStorage implements DataStorage<Match>
{
	@Override
	public Class<Match> dataType()
	{
		return Match.class;
	}

	public abstract List<Match> findAll(String groupId, String sportId, String playerId);

	public abstract boolean existsByGroupId(String groupId);

	public abstract boolean existsBySportId(String sportId);

	public abstract int longestWinStreak(String playerId, String sportId);
}
