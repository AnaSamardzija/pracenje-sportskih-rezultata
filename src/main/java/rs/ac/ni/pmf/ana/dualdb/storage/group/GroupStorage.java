package rs.ac.ni.pmf.ana.dualdb.storage.group;

import rs.ac.ni.pmf.ana.dualdb.model.Group;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;

import java.util.Collection;
import java.util.List;

public abstract class GroupStorage implements DataStorage<Group>
{
	@Override
	public Class<Group> dataType()
	{
		return Group.class;
	}

	public abstract List<Group> findAllById(Collection<String> ids);
}
