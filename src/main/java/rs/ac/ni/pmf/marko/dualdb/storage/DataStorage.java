package rs.ac.ni.pmf.marko.dualdb.storage;

import rs.ac.ni.pmf.marko.dualdb.data.StorageType;

import java.util.List;
import java.util.Optional;

public interface DataStorage<T>
{
	StorageType type();

	Class<T> dataType();

	List<T> findAll();

	Optional<T> findById(String id);

	T save(T entity);

	void deleteById(String id);

	Optional<T> findByUsername(String username);
}
