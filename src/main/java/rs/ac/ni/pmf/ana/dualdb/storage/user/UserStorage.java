package rs.ac.ni.pmf.ana.dualdb.storage.user;

import rs.ac.ni.pmf.ana.dualdb.model.User;
import rs.ac.ni.pmf.ana.dualdb.storage.DataStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class UserStorage implements DataStorage<User>
{
	@Override
	public Class<User> dataType()
	{
		return User.class;
	}

	public abstract List<User> findAllById(Collection<String> ids);

	public abstract Optional<User> findByUsername(String username);

	public abstract boolean existsByUsername(String username);

	public abstract boolean existsByEmail(String email);
}
