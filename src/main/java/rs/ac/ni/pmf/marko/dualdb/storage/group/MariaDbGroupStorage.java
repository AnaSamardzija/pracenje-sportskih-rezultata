package rs.ac.ni.pmf.marko.dualdb.storage.group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.GroupEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.mapper.MariaDbGroupMapper;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbGroupRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.marko.dualdb.model.Group;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
				.collect(Collectors.toList());
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
		final GroupEntity entity = group.getId() == null ? createNew(group) : update(group);
		return _groupMapper.toModel(_groupRepository.save(entity));
	}

	private GroupEntity createNew(final Group group)
	{
		final UserEntity createdBy = _userRepository.getReferenceById(Long.parseLong(group.getCreatedBy()));
		return _groupMapper.toEntity(group, createdBy);
	}

	private GroupEntity update(final Group group)
	{
		final GroupEntity existing = _groupRepository.findById(Long.parseLong(group.getId()))
				.orElseThrow(() -> new IllegalArgumentException("Group with id " + group.getId() + " not found"));

		existing.setName(group.getName());
		existing.setDescription(group.getDescription());

		return existing;
	}

	@Override
	@Transactional
	public void deleteById(final String id)
	{
		_groupRepository.deleteById(Long.parseLong(id));
	}
}
