package rs.ac.ni.pmf.marko.dualdb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.Set;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UserDto
{
	String id;

	@NotBlank(message = "Username cannot be blank")
	String username;
	String firstName;
	String lastName;

	@Email(message = "Email is not valid")
	String email;

	@Builder.Default
	Set<String> roles = Collections.emptySet();
}
