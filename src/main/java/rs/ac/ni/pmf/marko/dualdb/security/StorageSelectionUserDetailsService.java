package rs.ac.ni.pmf.marko.dualdb.security;

import org.springframework.security.core.userdetails.UserDetails;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;

public interface StorageSelectionUserDetailsService
{
	UserDetails loadUserByUsername(String username, StorageType storageType);
}
