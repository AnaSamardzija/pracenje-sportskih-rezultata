package rs.ac.ni.pmf.marko.dualdb.storage.sport;

import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;

import java.util.List;
import java.util.Optional;

public abstract class SportStorage implements DataStorage<Sport>
{
	@Override
	public Class<Sport> dataType()
	{
		return Sport.class;
	}

	public abstract List<Sport> findAllActive();

	public abstract Optional<Sport> findByName(String name);
}
