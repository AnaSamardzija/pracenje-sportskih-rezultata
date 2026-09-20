package rs.ac.ni.pmf.marko.dualdb.storage.match;

import rs.ac.ni.pmf.marko.dualdb.model.Match;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;

public abstract class MatchStorage implements DataStorage<Match>
{
	@Override
	public Class<Match> dataType()
	{
		return Match.class;
	}
}
