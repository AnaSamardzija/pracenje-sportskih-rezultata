package rs.ac.ni.pmf.ana.dualdb.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.RoleDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.UserDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper.MongoUserMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoRoleRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoUserRepository;
import rs.ac.ni.pmf.ana.dualdb.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MongoDbUserStorage extends UserStorage
{
	private final MongoUserRepository _userRepository;
	private final MongoRoleRepository _roleRepository;

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
		final Set<RoleDocument> roles = user.getRoles().stream()
				.map(_roleRepository::findByName)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());

		final UserDocument document = _userMapper.toDocument(user, roles);
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

	@Override
	public boolean existsByUsername(final String username)
	{
		return _userRepository.existsByUsername(username);
	}

	@Override
	public boolean existsByEmail(final String email)
	{
		return _userRepository.existsByEmail(email);
	}
}
