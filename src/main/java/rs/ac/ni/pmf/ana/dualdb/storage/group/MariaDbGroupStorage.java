package rs.ac.ni.pmf.ana.dualdb.storage.group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.GroupEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.mapper.MariaDbGroupMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbGroupRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.ana.dualdb.model.Group;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MariaDbGroupStorage extends GroupStorage
{
	private final MariaDbGroupRepository _groupRepository;
	private final MariaDbUserRepository _userRepository;
	private final MariaDbGroupMapper _groupMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MARIADB;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Group> findAll()
	{
		return _groupRepository.findAll().stream()
				.map(_groupMapper::toModel)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Group> findById(final String id)
	{
		return _groupRepository.findById(Long.parseLong(id)).map(_groupMapper::toModel);
	}

	@Override
	@Transactional
	public Group save(final Group group)
	{
		final UserEntity createdBy = _userRepository.getReferenceById(Long.parseLong(group.getCreatedBy()));
		final GroupEntity saved = _groupRepository.save(_groupMapper.toEntity(group, createdBy));
		return _groupMapper.toModel(saved);
	}

	@Override
	@Transactional
	public void deleteById(final String id)
	{
		_groupRepository.deleteById(Long.parseLong(id));
	}
}
