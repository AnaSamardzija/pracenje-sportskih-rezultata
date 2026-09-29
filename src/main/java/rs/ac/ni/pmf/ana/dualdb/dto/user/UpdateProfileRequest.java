package rs.ac.ni.pmf.ana.dualdb.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UpdateProfileRequest
{
	@Size(max = 100, message = "firstName must be at most 100 characters")
	@Schema(example = "Petar")
	String firstName;
	@Size(max = 100, message = "lastName must be at most 100 characters")
	@Schema(example = "Peric")
	String lastName;

	@NotBlank(message = "email is required")
	@Email(message = "email is not valid")
	@Size(max = 150, message = "email must be at most 150 characters")
	@Schema(example = "petar@example.com")
	String email;
}
