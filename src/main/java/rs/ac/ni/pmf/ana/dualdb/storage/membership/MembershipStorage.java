package rs.ac.ni.pmf.ana.dualdb.storage.membership;

import rs.ac.ni.pmf.ana.dualdb.model.Membership;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;

import java.util.List;
import java.util.Optional;

public abstract class MembershipStorage implements DataStorage<Membership>
{
	@Override
	public Class<Membership> dataType()
	{
		return Membership.class;
	}

	public abstract List<Membership> findByGroupId(String groupId);

	public abstract List<Membership> findByUserId(String userId);

	public abstract Optional<Membership> findByUserIdAndGroupId(String userId, String groupId);
}
