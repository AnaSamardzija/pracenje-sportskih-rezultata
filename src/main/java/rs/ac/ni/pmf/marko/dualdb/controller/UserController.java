package rs.ac.ni.pmf.marko.dualdb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.marko.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.RankingMapper;
import rs.ac.ni.pmf.marko.dualdb.dto.ranking.PlayerStatsResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.user.ChangePasswordRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.user.UpdateProfileRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.user.UserDto;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.UserMapper;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.marko.dualdb.service.RankingService;
import rs.ac.ni.pmf.marko.dualdb.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController
{
	private final UserMapper _userMapper;
	private final UserService _userService;
	private final RankingMapper _rankingMapper;
	private final RankingService _rankingService;

	@GetMapping("/me")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Moj profil (prijavljen korisnik)")
	@ApiResponse(responseCode = "200", description = "Profil prijavljenog korisnika")
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public UserDto getCurrentUser(@AuthenticationPrincipal final CustomUserDetails principal)
	{
		return _userMapper.toDto(principal.getUser());
	}

	@PutMapping("/me")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Izmena mog profila (prijavljen korisnik)",
			description = "Menjaju se ime, prezime i email; izostavljeno ime ili prezime postaje null. "
					+ "Korisničko ime i uloge se ovde ne menjaju.")
	@ApiResponse(responseCode = "200", description = "Profil je izmenjen")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "Email koristi drugi korisnik",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public UserDto updateCurrentUser(@AuthenticationPrincipal final CustomUserDetails principal,
	                                 @RequestBody @Valid final UpdateProfileRequest request)
	{
		return _userMapper.toDto(_userService.updateProfile(principal.getUsername(), _userMapper.toModel(request)));
	}

	@PutMapping("/me/password")
	@Tag(name = "Nalog i korisnici")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Promena moje lozinke (prijavljen korisnik)",
			description = "Traži staru lozinku; posle promene prijava radi samo sa novom.")
	@ApiResponse(responseCode = "204", description = "Lozinka je promenjena")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Stara lozinka nije tačna",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void changePassword(@AuthenticationPrincipal final CustomUserDetails principal,
	                           @RequestBody @Valid final ChangePasswordRequest request)
	{
		_userService.changePassword(principal.getUsername(), request.getOldPassword(), request.getNewPassword());
	}

	@GetMapping("/me/stats")
	@Tag(name = "Rang-liste i statistika")
	@Operation(summary = "Moja statistika (prijavljen korisnik)",
			description = "Pobede, nerešeni, porazi, bodovi, procenat pobeda i najduži niz uzastopnih pobeda iz svih "
					+ "mojih mečeva; ?sportId= ograničava statistiku na jedan sport.")
	@ApiResponse(responseCode = "200", description = "Statistika prijavljenog korisnika")
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Sport ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public PlayerStatsResponse getCurrentUserStats(@AuthenticationPrincipal final CustomUserDetails principal,
	                                               @RequestParam(required = false) final String sportId)
	{
		return _rankingMapper.toResponse(_rankingService.playerStats(principal.getUser().getId(), sportId));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Korisnik po id-u (samo SYSTEM_ADMIN)")
	@ApiResponse(responseCode = "200", description = "Korisnik")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Korisnik ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public UserDto getUserById(@PathVariable final String id)
	{
		return _userMapper.toDto(_userService.findById(id));
	}

	@GetMapping("/{id}/stats")
	@Tag(name = "Rang-liste i statistika")
	@Operation(summary = "Statistika igrača (prijavljen korisnik)",
			description = "Pobede, nerešeni, porazi, bodovi, procenat pobeda i najduži niz uzastopnih pobeda igrača; "
					+ "?sportId= ograničava statistiku na jedan sport. Igrač bez odigranih mečeva vraća sve nule.")
	@ApiResponse(responseCode = "200", description = "Statistika igrača")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Korisnik ili sport ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public PlayerStatsResponse getUserStats(@PathVariable final String id,
	                                        @RequestParam(required = false) final String sportId)
	{
		return _rankingMapper.toResponse(_rankingService.playerStats(id, sportId));
	}

	@GetMapping
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Spisak svih korisnika (samo SYSTEM_ADMIN)")
	@ApiResponse(responseCode = "200", description = "Spisak korisnika")
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public List<UserDto> getAllUsers()
	{
		return _userService.findAll().stream()
				.map(_userMapper::toDto)
				.collect(Collectors.toList());
	}
}
