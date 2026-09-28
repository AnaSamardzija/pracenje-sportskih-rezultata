package rs.ac.ni.pmf.ana.dualdb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.dualdb.dto.auth.AuthRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.auth.AuthResponse;
import rs.ac.ni.pmf.ana.dualdb.dto.auth.RegisterRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.ana.dualdb.dto.mapper.UserMapper;
import rs.ac.ni.pmf.ana.dualdb.service.auth.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
@SecurityRequirements
public class AuthController
{
	private final AuthService _authService;
	private final UserMapper _userMapper;

	@PostMapping("/login")
	@Operation(summary = "Prijava (javna)",
			description = "Vraća JWT token za izabranu bazu (storageType: MARIADB ili MONGODB); sve naredne rute rade nad tom bazom. "
					+ "Token se unosi kroz dugme Authorize. Korisničko ime ne razlikuje velika i mala slova.")
	@ApiResponse(responseCode = "200", description = "Prijava je uspela, vraća token")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva (npr. bez storageType)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Pogrešno korisničko ime ili lozinka, ili je nalog deaktiviran",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public AuthResponse login(@RequestBody @Valid final AuthRequest authRequest)
	{
		return _authService.authenticate(
				authRequest.getUsername(),
				authRequest.getPassword(),
				authRequest.getStorageType());
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Registracija novog korisnika (javna)",
			description = "Novi korisnik dobija ulogu USER i odmah je prijavljen: vraća token kao i prijava. "
					+ "firstName i lastName nisu obavezni.")
	@ApiResponse(responseCode = "201", description = "Korisnik je registrovan, vraća token")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "Korisničko ime ili email su zauzeti",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public AuthResponse register(@RequestBody @Valid final RegisterRequest registerRequest)
	{
		return _authService.register(_userMapper.toModel(registerRequest), registerRequest.getStorageType());
	}
}
