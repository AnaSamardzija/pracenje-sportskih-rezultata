package rs.ac.ni.pmf.marko.dualdb.data.mongodb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.UserDocument;
import rs.ac.ni.pmf.marko.dualdb.model.User;

import java.time.LocalDateTime;

@Component
public class MongoUserMapper
{
	public User toUser(final UserDocument userDocument)
	{
		return User.builder()
				.id(userDocument.getId())
				.firstName(userDocument.getFirstName())
				.lastName(userDocument.getLastName())
				.username(userDocument.getUsername())
				.password(userDocument.getPassword())
				.email(userDocument.getEmail())
				.roles(userDocument.getRoles())
				.permissions(userDocument.getPermissions())
				.build();
	}

	public UserDocument toDocument(final User user)
	{
		return UserDocument.builder()
				.id(user.getId())
				.username(user.getUsername())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.password(user.getPassword())
				.email(user.getEmail())
				.roles(user.getRoles())
				.permissions(user.getPermissions())
				.createdAt(LocalDateTime.now())
				.build();
	}
}
