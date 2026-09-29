package rs.ac.ni.pmf.ana.dualdb.dto.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.dto.auth.RegisterRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.user.UpdateProfileRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.user.UpdateUserRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.user.UserDto;
import rs.ac.ni.pmf.ana.dualdb.model.User;

import java.util.HashSet;

@Component
public class UserMapper
{
	public UserDto toDto(final User user)
	{
		return UserDto.builder()
				.id(user.getId())
				.username(user.getUsername())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.roles(user.getRoles())
				.permissions(user.getPermissions())
				.active(user.isActive())
				.build();
	}

	public User toModel(final RegisterRequest request)
	{
		return User.builder()
				.username(request.getUsername())
				.password(request.getPassword())
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.build();
	}

	public User toModel(final UpdateProfileRequest request)
	{
		return User.builder()
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.build();
	}

	public User toModel(final UpdateUserRequest request)
	{
		return User.builder()
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.roles(new HashSet<>(request.getRoles()))
				.build();
	}
}
