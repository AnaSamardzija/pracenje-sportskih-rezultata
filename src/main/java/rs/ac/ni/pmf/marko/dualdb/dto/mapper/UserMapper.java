package rs.ac.ni.pmf.marko.dualdb.dto.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.dto.UserDto;
import rs.ac.ni.pmf.marko.dualdb.model.User;

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
				.build();
	}
}
