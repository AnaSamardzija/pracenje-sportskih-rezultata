package rs.ac.ni.pmf.marko.dualdb.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.UserDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.mapper.MongoUserMapper;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository.MongoUserRepository;
import rs.ac.ni.pmf.marko.dualdb.model.User;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class MongoDbUserStorage extends UserStorage
{
	private final MongoUserRepository _userRepository;
	private final MongoUserMapper _userMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MONGODB;
	}

	@Override
	public List<User> findAll()
	{
		return _userRepository.findAll().stream().map(_userMapper::toUser).toList();
	}

	@Override
	public Optional<User> findById(final String id)
	{
		return _userRepository.findById(id).map(_userMapper::toUser);
	}

	@Override
	public User save(final User user)
	{
		// Should figure out how to handle permissions
		final UserDocument document = _userMapper.toDocument(user);
		final UserDocument saved = _userRepository.save(document);
		return _userMapper.toUser(saved);
	}

	@Override
	public void deleteById(final String id)
	{
		_userRepository.deleteById(id);
	}

	@Override
	public Optional<User> findByUsername(final String username)
	{
		return _userRepository.findByUsername(username).map(_userMapper::toUser);
	}
}
