package rs.ac.ni.pmf.marko.dualdb.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ChangePasswordRequest
{
	@NotBlank(message = "oldPassword is required")
	String oldPassword;

	@NotBlank(message = "newPassword is required")
	@Size(min = 4, message = "newPassword must be at least 4 characters")
	String newPassword;
}
