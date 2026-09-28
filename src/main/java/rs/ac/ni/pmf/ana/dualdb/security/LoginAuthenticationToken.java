package rs.ac.ni.pmf.ana.dualdb.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;

public class LoginAuthenticationToken extends UsernamePasswordAuthenticationToken
{
	private final StorageType _storageType;

	public LoginAuthenticationToken(
			final String username,
			final String password,
			final StorageType storageType)
	{
		super(username, password);
		_storageType = storageType;
	}

	public StorageType getStorageType()
	{
		return _storageType;
	}
}
