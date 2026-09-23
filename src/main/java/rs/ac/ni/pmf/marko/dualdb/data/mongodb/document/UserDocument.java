package rs.ac.ni.pmf.marko.dualdb.data.mongodb.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument
{
	@Id
	private String id;

	private String firstName;
	private String lastName;

	@Indexed(name = "uk_users_username", unique = true, collation = "{ 'locale': 'en', 'strength': 1 }")
	private String username;

	private String password;

	@Indexed(name = "uk_users_email", unique = true, collation = "{ 'locale': 'en', 'strength': 1 }")
	private String email;

	@Builder.Default
	private Set<String> roles = new HashSet<>();

	@Builder.Default
	private Set<String> permissions = new HashSet<>();

	private LocalDateTime createdAt;
}
