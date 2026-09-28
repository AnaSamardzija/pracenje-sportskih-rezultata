package rs.ac.ni.pmf.ana.dualdb.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;

@Value
@Builder
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuthRequest
{
	@NotBlank(message = "Username cannot be blank")
	@Schema(example = "admin")
	String username;

	@NotNull(message = "Password cannot be null")
	@Schema(example = "admin.123")
	String password;

	@NotNull(message = "storageType is required (MARIADB or MONGODB)")
	@Schema(example = "MARIADB")
	StorageType storageType;
}
