package rs.ac.ni.pmf.marko.dualdb.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse
{
	@NotBlank
	String accessToken;
}
