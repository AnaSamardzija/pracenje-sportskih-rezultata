package rs.ac.ni.pmf.marko.dualdb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.marko.dualdb.dto.auth.AuthRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.auth.AuthResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.auth.RegisterRequest;
import rs.ac.ni.pmf.marko.dualdb.service.auth.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController
{
	private final AuthService _authService;

	@PostMapping("/login")
	public AuthResponse login(@RequestBody @Valid final AuthRequest authRequest)
	{
		return _authService.authenticate(
				authRequest.getUsername(),
				authRequest.getPassword(),
				authRequest.getStorageType());
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@RequestBody @Valid final RegisterRequest registerRequest)
	{
		return _authService.register(registerRequest);
	}
}
