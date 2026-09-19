package rs.ac.ni.pmf.marko.dualdb.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.dto.auth.AuthResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.auth.RegisterRequest;
import rs.ac.ni.pmf.marko.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.marko.dualdb.model.User;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.marko.dualdb.security.JwtUtil;
import rs.ac.ni.pmf.marko.dualdb.security.LoginAuthenticationToken;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.marko.dualdb.storage.user.UserStorage;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService
{
	private final AuthenticationManager _authenticationManager;
	private final JwtUtil _jwtUtil;
	private final StorageResolver _storageResolver;
	private final PasswordEncoder _passwordEncoder;

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

	@Transactional
	public AuthResponse register(final RegisterRequest request)
	{
		final StorageType storageType = request.getStorageType();
		final UserStorage storage = (UserStorage) _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalArgumentException("Invalid storage type: " + storageType));

		if (storage.existsByUsername(request.getUsername()))
		{
			throw new DuplicateResourceException("Username already taken: " + request.getUsername());
		}

		if (storage.existsByEmail(request.getEmail()))
		{
			throw new DuplicateResourceException("Email already in use: " + request.getEmail());
		}

		final User newUser = User.builder()
				.username(request.getUsername())
				.password(_passwordEncoder.encode(request.getPassword()))
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.roles(Set.of("USER"))
				.build();

		final User saved = storage.save(newUser);

		final CustomUserDetails userDetails = new CustomUserDetails(saved, storageType);
		final String accessToken = _jwtUtil.generateToken(storageType, userDetails);

		return AuthResponse.builder()
				.accessToken(accessToken)
				.build();
	}
}
