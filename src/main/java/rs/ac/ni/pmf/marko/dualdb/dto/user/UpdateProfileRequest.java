package rs.ac.ni.pmf.marko.dualdb.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UpdateProfileRequest
{
	@Schema(example = "Petar")
	String firstName;
	@Schema(example = "Peric")
	String lastName;

	@NotBlank(message = "email is required")
	@Email(message = "email is not valid")
	@Schema(example = "petar@example.com")
	String email;
}
