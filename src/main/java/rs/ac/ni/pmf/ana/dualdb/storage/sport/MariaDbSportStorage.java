package rs.ac.ni.pmf.ana.dualdb.storage.sport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.SportEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.mapper.MariaDbSportMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbSportRepository;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MariaDbSportStorage extends SportStorage
{
	private final MariaDbSportRepository _sportRepository;
	private final MariaDbSportMapper _sportMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MARIADB;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Sport> findAll()
	{
		return _sportRepository.findAll().stream()
				.map(_sportMapper::toModel)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Sport> findAllActive()
	{
		return _sportRepository.findByActiveTrue().stream()
				.map(_sportMapper::toModel)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Sport> findByName(final String name)
	{
		return _sportRepository.findByNameIgnoreCase(name).map(_sportMapper::toModel);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Sport> findById(final String id)
	{
		return _sportRepository.findById(Long.parseLong(id)).map(_sportMapper::toModel);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Sport> findAllById(final Collection<String> ids)
	{
		return _sportRepository.findAllById(ids.stream().map(Long::parseLong).toList()).stream()
				.map(_sportMapper::toModel)
				.toList();
	}

	@Override
	@Transactional
	public Sport save(final Sport sport)
	{
		final SportEntity saved = _sportRepository.save(_sportMapper.toEntity(sport));
		return _sportMapper.toModel(saved);
	}

	@Override
	@Transactional
	public void deleteById(final String id)
	{
		_sportRepository.deleteById(Long.parseLong(id));
	}
}
