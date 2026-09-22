package rs.ac.ni.pmf.marko.dualdb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import rs.ac.ni.pmf.marko.dualdb.dto.common.ErrorDto;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Neprijavljen zahtev (nema tokena, ili je token istekao/pokvaren) dobija 401 sa ErrorDto-om,
 * da bi frontend mogao da razlikuje „prijavi se ponovo" (401) od „nemaš pravo" (403).
 */
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint
{
	private final JsonMapper _responseMapper;

	@Override
	@NullMarked
	public void commence(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final AuthenticationException authException) throws IOException
	{
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		final ErrorDto error = ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message(authException.getMessage())
				.build();

		_responseMapper.writeValue(response.getOutputStream(), error);
	}
}
