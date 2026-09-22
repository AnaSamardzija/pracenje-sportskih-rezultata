package rs.ac.ni.pmf.marko.dualdb.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;

@Value
@Builder
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuthRequest
{
	@NotBlank(message = "Username cannot be blank")
	String username;

	@NotNull(message = "Password cannot be null")
	String password;

	@NotNull(message = "storageType is required (MARIADB or MONGODB)")
	StorageType storageType;
}
