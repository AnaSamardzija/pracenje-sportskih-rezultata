package rs.ac.ni.pmf.marko.dualdb.data.mariadb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.PermissionEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.RoleEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.model.User;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MariaDbUserMapper
{
	public User toUser(final UserEntity userEntity)
	{
		final Set<String> roles = userEntity.getRoles().stream()
				.map(RoleEntity::getName)
				.collect(Collectors.toSet());

		final Set<String> permissions = userEntity.getRoles().stream()
				.flatMap(role -> role.getPermissions().stream())
				.map(PermissionEntity::getName)
				.collect(Collectors.toSet());

		return User.builder()
				.id(String.valueOf(userEntity.getId()))
				.username(userEntity.getUsername())
				.password(userEntity.getPassword())
				.firstName(userEntity.getFirstName())
				.lastName(userEntity.getLastName())
				.email(userEntity.getEmail())
				.createdAt(userEntity.getCreatedAt())
				.active(userEntity.isActive())
				.roles(roles)
				.permissions(permissions)
				.build();
	}

	public UserEntity toEntity(final User user, final Set<RoleEntity> roles)
	{
		return UserEntity.builder()
				.username(user.getUsername())
				.password(user.getPassword())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.active(user.isActive())
				.roles(roles)
				.build();
	}
}
