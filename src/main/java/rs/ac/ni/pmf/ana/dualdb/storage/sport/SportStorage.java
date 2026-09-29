package rs.ac.ni.pmf.ana.dualdb.storage.sport;

import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class SportStorage implements DataStorage<Sport>
{
	@Override
	public Class<Sport> dataType()
	{
		return Sport.class;
	}

	public abstract List<Sport> findAllById(Collection<String> ids);

	public abstract List<Sport> findAllActive();

	public abstract Optional<Sport> findByName(String name);
}
