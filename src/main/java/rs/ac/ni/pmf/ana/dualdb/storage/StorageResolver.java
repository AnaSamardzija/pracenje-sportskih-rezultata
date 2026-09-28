package rs.ac.ni.pmf.ana.dualdb.storage;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StorageResolver
{
	private final Map<StorageKey, DataStorage<?>> _storageMap;

	public StorageResolver(final List<DataStorage<?>> availableDataStorages)
	{
		_storageMap = availableDataStorages.stream()
				.collect(Collectors.toMap(
						dataStorage -> new StorageKey(dataStorage.type(), dataStorage.dataType()),
						Function.identity()
				));
	}

	public <T> Optional<DataStorage<T>> resolve(final StorageType storageType, final Class<T> dataType)
	{
		final StorageKey key = new StorageKey(storageType, dataType);
		return Optional.ofNullable((DataStorage<T>) _storageMap.get(key));
	}

	private record StorageKey(StorageType storageType, Class<?> dataType) {}
}
