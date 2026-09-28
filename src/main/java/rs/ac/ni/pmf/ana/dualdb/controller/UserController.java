package rs.ac.ni.pmf.ana.dualdb.controller;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.ana.dualdb.dto.mapper.RankingMapper;
import rs.ac.ni.pmf.ana.dualdb.dto.ranking.PlayerStatsResponse;
import rs.ac.ni.pmf.ana.dualdb.dto.user.ChangePasswordRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.user.UpdateProfileRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.user.UpdateUserRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.user.UserDto;
import rs.ac.ni.pmf.ana.dualdb.dto.mapper.UserMapper;
import rs.ac.ni.pmf.ana.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.ana.dualdb.service.RankingService;
import rs.ac.ni.pmf.ana.dualdb.service.UserService;

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
	@ApiResponse(responseCode = "400", description = "Nenumerički id u filteru (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
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
	@PreAuthorize("hasRole('SYSTEM_ADMIN') or #id == principal.user.id")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Korisnik po id-u (SYSTEM_ADMIN ili sam korisnik)")
	@ApiResponse(responseCode = "200", description = "Korisnik")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN, a traži tuđi nalog",
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
	@Operation(summary = "Spisak svih ostalih korisnika (samo SYSTEM_ADMIN)",
			description = "Vraća sve korisnike osim samog admina koji pita (sebe vidi preko /users/me), "
					+ "uključujući i deaktivirane (active=false).")
	@ApiResponse(responseCode = "200", description = "Spisak korisnika")
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public List<UserDto> getAllUsers(@AuthenticationPrincipal final CustomUserDetails principal)
	{
		return _userService.findAllExcept(principal.getUser().getId()).stream()
				.map(_userMapper::toDto)
				.collect(Collectors.toList());
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Izmena korisnika (samo SYSTEM_ADMIN)",
			description = "Menjaju se ime, prezime, email i uloge; izostavljeno ime ili prezime postaje null. "
					+ "Korisničko ime se ne menja. Admin sebi ne može da skine ulogu SYSTEM_ADMIN.")
	@ApiResponse(responseCode = "200", description = "Korisnik je izmenjen")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva (npr. nepoznata uloga) ili nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Korisnik ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "Email koristi drugi korisnik",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Admin pokušava sebi da skine ulogu SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public UserDto updateUser(@AuthenticationPrincipal final CustomUserDetails principal,
	                          @PathVariable final String id,
	                          @RequestBody @Valid final UpdateUserRequest request)
	{
		return _userMapper.toDto(_userService.update(id, _userMapper.toModel(request), principal.getUser().getId()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Deaktivacija korisnika (samo SYSTEM_ADMIN)",
			description = "Korisnik se ne briše iz baze, već postaje neaktivan: ne može da se prijavi, a token koji "
					+ "već ima prestaje da važi. Ostaje u mečevima, rang-listama i grupama, a u grupama gde je bio GROUP_ADMIN "
					+ "admin postaje aktivan član koji je najduže u grupi, ako takav postoji. Vraća se preko PATCH /restore.")
	@ApiResponse(responseCode = "204", description = "Korisnik je deaktiviran")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Korisnik ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Korisnik je već deaktiviran ili admin deaktivira sebe",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void deactivateUser(@AuthenticationPrincipal final CustomUserDetails principal,
	                           @PathVariable final String id)
	{
		_userService.deactivate(id, principal.getUser().getId());
	}

	@PatchMapping("/{id}/restore")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Tag(name = "Nalog i korisnici")
	@Operation(summary = "Vraćanje deaktiviranog korisnika (samo SYSTEM_ADMIN)")
	@ApiResponse(responseCode = "200", description = "Korisnik je ponovo aktivan")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Korisnik ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Korisnik je već aktivan",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public UserDto restoreUser(@PathVariable final String id)
	{
		return _userMapper.toDto(_userService.restore(id));
	}
}
