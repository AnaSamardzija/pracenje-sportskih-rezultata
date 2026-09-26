package rs.ac.ni.pmf.marko.dualdb.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentStorageTypeProvider
{
	public StorageType getCurrentStorageType()
	{
		final Authentication authentication = SecurityContextHolder
				.getContext()
				.getAuthentication();

		if (authentication == null)
		{
			throw new IllegalStateException("Authentication not found");
		}

		if (authentication.getPrincipal() instanceof final CustomUserDetails userDetails)
		{
			return userDetails.getStorageType();
		}

		log.error("Storage type not recognized, using MARIADB.");
		return StorageType.MARIADB;
	}
}
