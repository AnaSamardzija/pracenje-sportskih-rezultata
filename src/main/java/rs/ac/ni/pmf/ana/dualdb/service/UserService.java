package rs.ac.ni.pmf.ana.dualdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Permission;
import rs.ac.ni.pmf.ana.dualdb.model.User;
import rs.ac.ni.pmf.ana.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.ana.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.ana.dualdb.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;
	private final PasswordEncoder _passwordEncoder;
	private final MembershipService _membershipService;

	public List<User> findAllExcept(final String userId)
	{
		return userStorage().findAll().stream()
				.filter(user -> !user.getId().equals(userId))
				.toList();
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
		final User saved = storage.save(user);

		log.info("Profile of user {} updated", saved.getUsername());
		return saved;
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

		log.info("Password of user {} changed", user.getUsername());
	}

	@Transactional
	public User update(final String id, final User changes, final User currentUser)
	{
		final UserStorage storage = userStorage();
		final User user = findById(id);

		if (!changes.getRoles().equals(user.getRoles()) && !currentUser.hasPermission(Permission.USERS_ROLES_ASSIGN))
		{
			throw new AccessDeniedException("You are not allowed to change user roles");
		}

		if (user.getId().equals(currentUser.getId())
				&& user.getRoles().contains("SYSTEM_ADMIN")
				&& !changes.getRoles().contains("SYSTEM_ADMIN"))
		{
			throw new InvalidOperationException("You cannot remove your own SYSTEM_ADMIN role");
		}

		if (!changes.getEmail().equalsIgnoreCase(user.getEmail()) && storage.existsByEmail(changes.getEmail()))
		{
			throw new DuplicateResourceException("Email already in use: " + changes.getEmail());
		}

		user.setFirstName(changes.getFirstName());
		user.setLastName(changes.getLastName());
		user.setEmail(changes.getEmail());
		user.setRoles(changes.getRoles());
		final User saved = storage.save(user);

		log.info("User {} '{}' updated, roles {}", saved.getId(), saved.getUsername(), saved.getRoles());
		return saved;
	}

	@Transactional
	public void deactivate(final String id, final String currentUserId)
	{
		final User user = findById(id);

		if (user.getId().equals(currentUserId))
		{
			throw new InvalidOperationException("You cannot deactivate your own account");
		}

		if (!user.isActive())
		{
			throw new InvalidOperationException("User with id " + id + " is already deactivated");
		}

		user.setActive(false);
		userStorage().save(user);

		log.info("User {} '{}' deactivated", user.getId(), user.getUsername());
		_membershipService.ensureActiveAdminInGroupsOf(user.getId());
	}

	@Transactional
	public User restore(final String id)
	{
		final User user = findById(id);

		if (user.isActive())
		{
			throw new InvalidOperationException("User with id " + id + " is already active");
		}

		user.setActive(true);
		final User saved = userStorage().save(user);

		log.info("User {} '{}' restored", saved.getId(), saved.getUsername());
		_membershipService.ensureActiveAdminInGroupsOf(saved.getId());

		return saved;
	}

	private UserStorage userStorage()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		return (UserStorage) _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));
	}
}
