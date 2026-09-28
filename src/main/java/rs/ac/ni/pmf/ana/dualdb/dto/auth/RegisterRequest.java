package rs.ac.ni.pmf.ana.dualdb.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RegisterRequest
{
	@NotBlank(message = "username is required")
	@Schema(example = "pera")
	String username;

	@NotBlank(message = "password is required")
	@Size(min = 4, message = "password must be at least 4 characters")
	@Schema(example = "pera.123")
	String password;

	@Schema(example = "Pera")
	String firstName;
	@Schema(example = "Peric")
	String lastName;

	@NotBlank(message = "email is required")
	@Email(message = "email is not valid")
	@Schema(example = "pera@example.com")
	String email;

	@NotNull(message = "storageType is required (MARIADB or MONGODB)")
	@Schema(example = "MARIADB")
	StorageType storageType;
}
