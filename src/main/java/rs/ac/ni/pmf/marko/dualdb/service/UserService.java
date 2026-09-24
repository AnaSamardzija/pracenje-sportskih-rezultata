package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.marko.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.User;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.marko.dualdb.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;
	private final PasswordEncoder _passwordEncoder;

	public List<User> findAll()
	{
		return userStorage().findAll();
	}

	public User findById(final String id)
	{
		return userStorage().findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
	}

	@Transactional
	public User updateProfile(final String username, final User profile)
	{
		final UserStorage storage = userStorage();
		final User user = storage.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

		if (!profile.getEmail().equalsIgnoreCase(user.getEmail()) && storage.existsByEmail(profile.getEmail()))
		{
			throw new DuplicateResourceException("Email already in use: " + profile.getEmail());
		}

		user.setFirstName(profile.getFirstName());
		user.setLastName(profile.getLastName());
		user.setEmail(profile.getEmail());

		return storage.save(user);
	}

	@Transactional
	public void changePassword(final String username, final String oldPassword, final String newPassword)
	{
		final UserStorage storage = userStorage();
		final User user = storage.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

		if (!_passwordEncoder.matches(oldPassword, user.getPassword()))
		{
			throw new InvalidOperationException("Old password is incorrect");
		}

		user.setPassword(_passwordEncoder.encode(newPassword));
		storage.save(user);
	}

	private UserStorage userStorage()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return (UserStorage) _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
