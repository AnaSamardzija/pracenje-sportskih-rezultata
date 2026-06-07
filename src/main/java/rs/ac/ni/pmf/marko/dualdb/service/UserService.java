package rs.ac.ni.pmf.marko.dualdb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.model.User;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.DataStorage;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService
{
	private final StorageResolver _storageResolver;
	private final CurrentStorageTypeProvider _storageTypeProvider;

	public List<User> findAll()
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		final DataStorage<User> dataStorage = _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));

		return dataStorage.findAll();
	}

	public User findById(final String id)
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		final DataStorage<User> dataStorage = _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));

		return dataStorage.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("User with id: " + id + " not found"));
	}

	public User save(final User user)
	{
		final StorageType storageType = _storageTypeProvider.getCurrentStorageType();
		final DataStorage<User> dataStorage = _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalStateException("No storage resolver found for type: " + storageType));

		return dataStorage.save(user);
	}
}
