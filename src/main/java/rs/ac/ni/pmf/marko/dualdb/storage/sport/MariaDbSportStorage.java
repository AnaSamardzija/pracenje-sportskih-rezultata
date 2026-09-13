package rs.ac.ni.pmf.marko.dualdb.storage.sport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.SportEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.mapper.MariaDbSportMapper;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbSportRepository;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Sport> findById(final String id)
	{
		return _sportRepository.findById(Long.parseLong(id)).map(_sportMapper::toModel);
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
