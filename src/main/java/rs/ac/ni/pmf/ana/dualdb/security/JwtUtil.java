package rs.ac.ni.pmf.ana.dualdb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil
{
	public static final String STORAGE_TYPE_KEY = "storageType";

	@Value("${app.jwt.secret}")
	private String _secretKey;

	@Value("${app.jwt.expiration-ms}")
	private Long _expirationMs;

	@PostConstruct
	void validateSigningKey()
	{
		getSigningKey();
	}

	private SecretKey getSigningKey()
	{
		final byte[] encodedKey = Decoders.BASE64.decode(_secretKey);
		return Keys.hmacShaKeyFor(encodedKey);
	}

	private Claims extractAllClaims(final String token)
	{
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver)
	{
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String extractUsername(final String token)
	{
		return extractClaim(token, Claims::getSubject);
	}

	private Date extractExpiration(final String token)
	{
		return extractClaim(token, Claims::getExpiration);
	}

	public Boolean isTokenExpired(final String token)
	{
		return extractExpiration(token).before(new Date());
	}

	public String generateToken(final StorageType storageType, final UserDetails userDetails)
	{
		final Date now = new Date();

		return Jwts.builder()
				.claims(Map.of(STORAGE_TYPE_KEY, storageType.name()))
				.subject(userDetails.getUsername())
				.issuedAt(now)
				.expiration(new Date(now.getTime() + _expirationMs))
				.signWith(getSigningKey())
				.compact();
	}

	public Boolean validateToken(
			final String token,
			final StorageType storageType,
			final UserDetails userDetails)
	{
		final String username = extractUsername(token);
		final StorageType storageTypeFromToken = extractStorageType(token);
		return (storageTypeFromToken == storageType
				&& username.equals(userDetails.getUsername())
				&& !isTokenExpired(token));
	}

	public StorageType extractStorageType(final String jwt)
	{
		final String storageTypeString = extractAllClaims(jwt).get(STORAGE_TYPE_KEY, String.class);
		return StorageType.valueOf(storageTypeString);
	}
}
