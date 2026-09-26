package rs.ac.ni.pmf.marko.dualdb.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UpdateUserRequest
{
	@Schema(example = "Petar")
	String firstName;
	@Schema(example = "Peric")
	String lastName;

	@NotBlank(message = "email is required")
	@Email(message = "email is not valid")
	@Schema(example = "petar@example.com")
	String email;

	@NotEmpty(message = "roles are required")
	@Schema(example = "[\"USER\", \"SYSTEM_ADMIN\"]")
	Set<@Pattern(regexp = "USER|SYSTEM_ADMIN", message = "role must be USER or SYSTEM_ADMIN") String> roles;
}
