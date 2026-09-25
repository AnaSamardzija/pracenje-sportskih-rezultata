package rs.ac.ni.pmf.marko.dualdb.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig
{
	private final JwtUtil _jwtUtil;
	private final StorageSelectionUserDetailsService _userDetailsService;
	private final JsonMapper _jsonMapper;

	@Bean
	public AuthenticationManager authenticationManager(final AuthenticationConfiguration configuration)
	{
		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint()
	{
		return new RestAuthenticationEntryPoint(_jsonMapper);
	}

	@Bean
	public SecurityFilterChain filterChain(final HttpSecurity http)
	{
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/login").permitAll()
						.requestMatchers("/api/v1/auth/register").permitAll()
						.requestMatchers("/api/v1/**").authenticated()
						.requestMatchers(
								"/",
								"/index.html",
								"/assets/**",
								"/*.js",
								"/*.css",
								"/doc",
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/favicon.ico"
						).permitAll()
						.anyRequest().permitAll()
				)
				.addFilterBefore(new JwtAuthenticationFilter(_jwtUtil, _userDetailsService),
				                 UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
