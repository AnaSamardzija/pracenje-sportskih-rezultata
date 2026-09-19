package rs.ac.ni.pmf.marko.dualdb.storage.user;

import rs.ac.ni.pmf.marko.dualdb.model.User;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;

import java.util.Optional;

public abstract class UserStorage implements DataStorage<User>
{
	@Override
	public Class<User> dataType()
	{
		return User.class;
	}

	public abstract Optional<User> findByUsername(String username);

	public abstract boolean existsByUsername(String username);

	public abstract boolean existsByEmail(String email);
}
