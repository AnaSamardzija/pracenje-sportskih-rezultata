package rs.ac.ni.pmf.ana.dualdb.storage.sport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.SportDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper.MongoSportMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoSportRepository;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoDbSportStorage extends SportStorage
{
	private final MongoSportRepository _sportRepository;
	private final MongoSportMapper _sportMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MONGODB;
	}

	@Override
	public List<Sport> findAll()
	{
		return _sportRepository.findAll().stream()
				.map(_sportMapper::toModel)
				.toList();
	}

	@Override
	public List<Sport> findAllActive()
	{
		return _sportRepository.findByActiveTrue().stream()
				.map(_sportMapper::toModel)
				.toList();
	}

	@Override
	public Optional<Sport> findByName(final String name)
	{
		return _sportRepository.findByName(name).map(_sportMapper::toModel);
	}

	@Override
	public Optional<Sport> findById(final String id)
	{
		return _sportRepository.findById(id).map(_sportMapper::toModel);
	}

	@Override
	public Sport save(final Sport sport)
	{
		final SportDocument saved = _sportRepository.save(_sportMapper.toDocument(sport));
		return _sportMapper.toModel(saved);
	}

	@Override
	public void deleteById(final String id)
	{
		_sportRepository.deleteById(id);
	}
}
