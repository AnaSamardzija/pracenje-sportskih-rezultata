package rs.ac.ni.pmf.ana.dualdb.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class ChangePasswordRequest
{
	@NotBlank(message = "oldPassword is required")
	@Schema(example = "pera.123")
	String oldPassword;

	@NotBlank(message = "newPassword is required")
	@Size(min = 4, max = 24, message = "newPassword must be between 4 and 24 characters")
	@Schema(example = "pera.456")
	String newPassword;
}
