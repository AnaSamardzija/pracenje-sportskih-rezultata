package rs.ac.ni.pmf.marko.dualdb.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class RegisterRequest
{
	@NotBlank(message = "username is required")
	String username;

	@NotBlank(message = "password is required")
	@Size(min = 4, message = "password must be at least 4 characters")
	String password;

	String firstName;
	String lastName;

	@NotBlank(message = "email is required")
	@Email(message = "email is not valid")
	String email;

	@NotNull(message = "storageType is required (MARIADB or MONGODB)")
	StorageType storageType;
}
