package rs.ac.ni.pmf.marko.dualdb.storage.group;

import rs.ac.ni.pmf.marko.dualdb.model.Group;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;

public abstract class GroupStorage implements DataStorage<Group>
{
	@Override
	public Class<Group> dataType()
	{
		return Group.class;
	}
}
