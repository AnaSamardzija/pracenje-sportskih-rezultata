package rs.ac.ni.pmf.ana.dualdb.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.dto.auth.AuthResponse;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.model.User;
import rs.ac.ni.pmf.ana.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.ana.dualdb.security.JwtUtil;
import rs.ac.ni.pmf.ana.dualdb.security.LoginAuthenticationToken;
import rs.ac.ni.pmf.ana.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.ana.dualdb.storage.user.UserStorage;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService
{
	private final AuthenticationManager _authenticationManager;
	private final JwtUtil _jwtUtil;
	private final StorageResolver _storageResolver;
	private final PasswordEncoder _passwordEncoder;

	public AuthResponse authenticate(final String username, final String password, final StorageType storageType)
	{
		final Authentication authentication = _authenticationManager
				.authenticate(new LoginAuthenticationToken(username.strip(), password, storageType));

		if (authentication.getPrincipal() instanceof final CustomUserDetails userDetails)
		{
			final String accessToken = _jwtUtil.generateToken(storageType, userDetails);

			log.info("User '{}' logged in ({})", userDetails.getUsername(), storageType);
			return AuthResponse.builder()
					.accessToken(accessToken)
					.build();
		}

		throw new IllegalStateException("Invalid user authentication");
	}

	@Transactional
	public AuthResponse register(final User user, final StorageType storageType)
	{
		final UserStorage storage = (UserStorage) _storageResolver.resolve(storageType, User.class)
				.orElseThrow(() -> new IllegalArgumentException("Invalid storage type: " + storageType));

		user.setUsername(user.getUsername().strip());

		if (storage.existsByUsername(user.getUsername()))
		{
			throw new DuplicateResourceException("Username already taken: " + user.getUsername());
		}

		if (storage.existsByEmail(user.getEmail()))
		{
			throw new DuplicateResourceException("Email already in use: " + user.getEmail());
		}

		user.setPassword(_passwordEncoder.encode(user.getPassword()));
		user.setRoles(Set.of("USER"));
		user.setActive(true);

		final User saved = storage.save(user);
		log.info("User {} '{}' registered ({})", saved.getId(), saved.getUsername(), storageType);

		final CustomUserDetails userDetails = new CustomUserDetails(saved, storageType);
		final String accessToken = _jwtUtil.generateToken(storageType, userDetails);

		return AuthResponse.builder()
				.accessToken(accessToken)
				.build();
	}
}
