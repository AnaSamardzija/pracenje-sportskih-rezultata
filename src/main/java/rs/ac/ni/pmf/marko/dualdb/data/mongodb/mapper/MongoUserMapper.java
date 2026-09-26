package rs.ac.ni.pmf.marko.dualdb.data.mongodb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.PermissionDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.RoleDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.UserDocument;
import rs.ac.ni.pmf.marko.dualdb.model.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MongoUserMapper
{
	public User toUser(final UserDocument userDocument)
	{
		final Set<String> roles = userDocument.getRoles().stream()
				.map(RoleDocument::getName)
				.collect(Collectors.toSet());

		final Set<String> permissions = userDocument.getRoles().stream()
				.flatMap(role -> role.getPermissions().stream())
				.map(PermissionDocument::getName)
				.collect(Collectors.toSet());

		return User.builder()
				.id(userDocument.getId())
				.firstName(userDocument.getFirstName())
				.lastName(userDocument.getLastName())
				.username(userDocument.getUsername())
				.password(userDocument.getPassword())
				.email(userDocument.getEmail())
				.createdAt(userDocument.getCreatedAt())
				.active(userDocument.isActive())
				.roles(roles)
				.permissions(permissions)
				.build();
	}

	public UserDocument toDocument(final User user, final Set<RoleDocument> roles)
	{
		return UserDocument.builder()
				.id(user.getId())
				.username(user.getUsername())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.password(user.getPassword())
				.email(user.getEmail())
				.roles(roles)
				.createdAt(user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt())
				.active(user.isActive())
				.build();
	}
}
