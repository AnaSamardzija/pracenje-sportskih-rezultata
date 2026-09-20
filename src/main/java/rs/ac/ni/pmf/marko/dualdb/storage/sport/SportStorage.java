package rs.ac.ni.pmf.marko.dualdb.storage.sport;

import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;

import java.util.List;

public abstract class SportStorage implements DataStorage<Sport>
{
	@Override
	public Class<Sport> dataType()
	{
		return Sport.class;
	}

	public abstract List<Sport> findAllActive();
}
