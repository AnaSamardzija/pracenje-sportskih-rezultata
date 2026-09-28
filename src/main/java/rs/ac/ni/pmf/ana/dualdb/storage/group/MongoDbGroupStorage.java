package rs.ac.ni.pmf.ana.dualdb.storage.group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.GroupDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper.MongoGroupMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoGroupRepository;
import rs.ac.ni.pmf.ana.dualdb.model.Group;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MongoDbGroupStorage extends GroupStorage
{
	private final MongoGroupRepository _groupRepository;
	private final MongoGroupMapper _groupMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MONGODB;
	}

	@Override
	public List<Group> findAll()
	{
		return _groupRepository.findAll().stream()
				.map(_groupMapper::toModel)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Group> findById(final String id)
	{
		return _groupRepository.findById(id).map(_groupMapper::toModel);
	}

	@Override
	public Group save(final Group group)
	{
		final GroupDocument saved = _groupRepository.save(_groupMapper.toDocument(group));
		return _groupMapper.toModel(saved);
	}

	@Override
	public void deleteById(final String id)
	{
		_groupRepository.deleteById(id);
	}
}
