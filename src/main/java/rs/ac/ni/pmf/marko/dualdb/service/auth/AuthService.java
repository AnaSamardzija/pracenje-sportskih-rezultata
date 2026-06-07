package rs.ac.ni.pmf.marko.dualdb.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.dto.auth.AuthResponse;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.marko.dualdb.security.JwtUtil;
import rs.ac.ni.pmf.marko.dualdb.security.LoginAuthenticationToken;

@Service
@RequiredArgsConstructor
public class AuthService
{
	private final AuthenticationManager _authenticationManager;
	private final JwtUtil _jwtUtil;

	public AuthResponse authenticate(final String username, final String password, final StorageType storageType)
	{
		final Authentication authentication = _authenticationManager
				.authenticate(new LoginAuthenticationToken(username, password, storageType));

		if (authentication.getPrincipal() instanceof final CustomUserDetails userDetails)
		{
			final String accessToken = _jwtUtil.generateToken(storageType, userDetails);

			return AuthResponse.builder()
					.accessToken(accessToken)
					.build();
		}

		throw new IllegalStateException("Invalid user authentication");
	}
}
