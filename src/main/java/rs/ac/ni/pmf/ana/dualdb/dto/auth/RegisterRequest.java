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
	@Size(max = 100, message = "username must be at most 100 characters")
	@Schema(example = "pera")
	String username;

	@NotBlank(message = "password is required")
	@Size(min = 4, max = 24, message = "password must be between 4 and 24 characters")
	@Schema(example = "pera.123")
	String password;

	@Size(max = 100, message = "firstName must be at most 100 characters")
	@Schema(example = "Pera")
	String firstName;
	@Size(max = 100, message = "lastName must be at most 100 characters")
	@Schema(example = "Peric")
	String lastName;

	@NotBlank(message = "email is required")
	@Email(message = "email is not valid")
	@Size(max = 150, message = "email must be at most 150 characters")
	@Schema(example = "pera@example.com")
	String email;

	@NotNull(message = "storageType is required (MARIADB or MONGODB)")
	@Schema(example = "MARIADB")
	StorageType storageType;
}
