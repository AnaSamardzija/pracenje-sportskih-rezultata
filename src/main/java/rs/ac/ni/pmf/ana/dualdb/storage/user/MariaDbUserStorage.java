package rs.ac.ni.pmf.ana.dualdb.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.RoleEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.mapper.MariaDbUserMapper;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbRoleRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.ana.dualdb.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MariaDbUserStorage extends UserStorage
{
	private final MariaDbUserRepository _userRepository;
	private final MariaDbRoleRepository _roleRepository;

	private final MariaDbUserMapper _userMapper;

	@Override
	public StorageType type()
	{
		return StorageType.MARIADB;
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findAll()
	{
		return _userRepository.findAll().stream()
				.map(_userMapper::toUser)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findById(final String id)
	{
		return _userRepository.findById(Long.parseLong(id)).map(_userMapper::toUser);
	}

	@Override
	@Transactional
	public User save(final User user)
	{
		final UserEntity userEntity = user.getId() == null ? createNewUser(user) : updateUser(user);
		final UserEntity savedEntity = _userRepository.save(userEntity);
		return _userMapper.toUser(savedEntity);
	}

	private UserEntity createNewUser(final User user)
	{
		final Set<RoleEntity> roles = user.getRoles().stream()
				.map(_roleRepository::findByName)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());

		return _userMapper.toEntity(user, roles);
	}

	private UserEntity updateUser(final User user)
	{
		final Long userId = Long.parseLong(user.getId());
		final UserEntity userEntity = _userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

		userEntity.setUsername(user.getUsername());
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());
		userEntity.setEmail(user.getEmail());
		userEntity.setPassword(user.getPassword());
		userEntity.setActive(user.isActive());

		final Set<RoleEntity> roles = user.getRoles().stream()
				.map(_roleRepository::findByName)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());

		userEntity.getRoles().clear();
		userEntity.getRoles().addAll(roles);

		return userEntity;
	}

	@Override
	@Transactional
	public void deleteById(final String id)
	{
		_userRepository.deleteById(Long.parseLong(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByUsername(final String username)
	{
		return _userRepository.findByUsername(username).map(_userMapper::toUser);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsername(final String username)
	{
		return _userRepository.existsByUsername(username);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmail(final String email)
	{
		return _userRepository.existsByEmail(email);
	}
}
