package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User implements DataModel
{
	String id;

	String username;
	String firstName;
	String lastName;
	String email;

	String password;

	LocalDateTime createdAt;

	@Builder.Default
	Set<String> roles = new HashSet<>();
	@Builder.Default
	Set<String> permissions = new HashSet<>();
}
