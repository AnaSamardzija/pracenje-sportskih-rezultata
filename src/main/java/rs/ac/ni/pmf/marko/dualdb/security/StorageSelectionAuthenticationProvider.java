package rs.ac.ni.pmf.marko.dualdb.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;

@Component
@RequiredArgsConstructor
@NullMarked
public class StorageSelectionAuthenticationProvider implements AuthenticationProvider
{
	private final StorageSelectionUserDetailsService _userDetailsService;
	private final PasswordEncoder _passwordEncoder;

	@Override
	public @Nullable Authentication authenticate(final Authentication authentication) throws AuthenticationException
	{
		final LoginAuthenticationToken token = (LoginAuthenticationToken) authentication;

		final String username = token.getName();
		assert token.getCredentials() != null;
		final String rawPassword = token.getCredentials().toString();
		final StorageType storageType = token.getStorageType();

		// Nepostojeći korisnik dobija istu grešku kao pogrešna lozinka, da odgovor ne bi otkrio
		// koja korisnička imena postoje.
		final UserDetails userDetails;

		try
		{
			userDetails = _userDetailsService.loadUserByUsername(username, storageType);
		}
		catch (final UsernameNotFoundException ex)
		{
			throw new BadCredentialsException("Invalid username or password");
		}

		if (!_passwordEncoder.matches(rawPassword, userDetails.getPassword()))
		{
			throw new BadCredentialsException("Invalid username or password");
		}

		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(final Class<?> authentication)
	{
		return LoginAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
