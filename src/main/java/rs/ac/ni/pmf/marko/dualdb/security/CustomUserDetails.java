package rs.ac.ni.pmf.marko.dualdb.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails
{
	private final User user;
	private final StorageType storageType;

	@Override
	@NullMarked
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		final Set<GrantedAuthority> authorities = new HashSet<>();

		user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
		user.getPermissions()
				.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.toLowerCase())));

		return authorities;
	}

	@Override
	public @Nullable String getPassword()
	{
		return user.getPassword();
	}

	@Override
	@NullMarked
	public String getUsername()
	{
		return user.getUsername();
	}
}
