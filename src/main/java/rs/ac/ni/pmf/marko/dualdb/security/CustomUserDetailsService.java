package rs.ac.ni.pmf.marko.dualdb.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.model.User;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.marko.dualdb.storage.user.UserStorage;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements StorageSelectionUserDetailsService
{
	private final StorageResolver _storageResolver;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(final String username, final StorageType storageType)
	{
		final UserStorage storage = (UserStorage) _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalArgumentException("Invalid storage type: " + storageType));

		final User user = storage.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		return new CustomUserDetails(user, storageType);
	}
}
