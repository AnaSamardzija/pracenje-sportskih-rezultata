package rs.ac.ni.pmf.marko.dualdb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.marko.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.MatchMapper;
import rs.ac.ni.pmf.marko.dualdb.dto.match.MatchRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.match.MatchResponse;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.marko.dualdb.service.MatchService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Tag(name = "Mečevi")
public class MatchController
{
	private static final String SETS_EXAMPLE = """
			{
			  "sportId": "1",
			  "groupId": "1",
			  "playedAt": "2026-09-20T12:00:00",
			  "sides": [
			    { "playerIds": ["2"], "setScores": [6, 4, 6] },
			    { "playerIds": ["3"], "setScores": [3, 6, 2] }
			  ]
			}""";

	private static final String POINTS_EXAMPLE = """
			{
			  "sportId": "2",
			  "groupId": "1",
			  "playedAt": "2026-09-20T12:00:00",
			  "sides": [
			    { "playerIds": ["2"], "score": 3 },
			    { "playerIds": ["3"], "score": 3 }
			  ]
			}""";

	private static final String OUTCOME_EXAMPLE = """
			{
			  "sportId": "3",
			  "groupId": "1",
			  "playedAt": "2026-09-20T12:00:00",
			  "sides": [
			    { "playerIds": ["2"], "outcome": "WIN" },
			    { "playerIds": ["3"], "outcome": "LOSS" }
			  ]
			}""";

	private final MatchService _matchService;
	private final MatchMapper _matchMapper;

	@GetMapping
	@Operation(summary = "Spisak mečeva (prijavljen korisnik)",
			description = "Filteri ?groupId=, ?sportId= i ?playerId= nisu obavezni i kombinuju se. Sortirano po vremenu "
					+ "odigravanja, najnoviji prvi. Filter po obrisanom sportu radi.")
	@ApiResponse(responseCode = "200", description = "Spisak mečeva")
	@ApiResponse(responseCode = "400", description = "Nenumerički id u filteru (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa, sport ili igrač iz filtera ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public List<MatchResponse> getAll(@RequestParam(required = false) final String groupId,
	                                  @RequestParam(required = false) final String sportId,
	                                  @RequestParam(required = false) final String playerId)
	{
		return _matchService.findAll(groupId, sportId, playerId).stream()
				.map(_matchMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Meč po id-u (prijavljen korisnik)",
			description = "Vidi ga i onaj ko nije član grupe.")
	@ApiResponse(responseCode = "200", description = "Meč")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Meč ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public MatchResponse getById(@PathVariable final String id)
	{
		return _matchMapper.toResponse(_matchService.findById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Unos meča (član grupe)",
			description = "Meč unosi bilo koji član grupe, ne mora da igra, a svi igrači moraju biti članovi grupe. "
					+ "Oblik rezultata zavisi od sporta: SETS šalje setScores, POINTS score, a OUTCOME outcome. "
					+ "Pobednika, a za SETS i POINTS i outcome, određuje sistem. "
					+ "ID-jevi u primerima su MariaDB ID-jevi; za MongoDB se zamenjuju hex ID-jevima.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
			@ExampleObject(name = "Tenis (SETS)", value = SETS_EXAMPLE),
			@ExampleObject(name = "Fudbal (POINTS)", value = POINTS_EXAMPLE),
			@ExampleObject(name = "Sah (OUTCOME)", value = OUTCOME_EXAMPLE)}))
	@ApiResponse(responseCode = "201", description = "Meč je unet")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva (npr. nisu tačno 2 strane ili je playedAt u budućnosti)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije član grupe",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Sport ili grupa ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Rezultat ne odgovara pravilima sporta, igrač nije član grupe ili je deaktiviran, ili je sport obrisan",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public MatchResponse create(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @RequestBody @Valid final MatchRequest request)
	{
		return _matchMapper.toResponse(_matchService.create(_matchMapper.toModel(request), principal.getUser().getId()));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Izmena meča (učesnik meča ili GROUP_ADMIN)",
			description = "Telo i provere rezultata su isti kao kod unosa. Meč ne može da se premesti u drugu grupu; sport "
					+ "može da se promeni, ali oblik rezultata mora da odgovara novom sportu. SYSTEM_ADMIN nema posebna prava.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
			@ExampleObject(name = "Tenis (SETS)", value = SETS_EXAMPLE),
			@ExampleObject(name = "Fudbal (POINTS)", value = POINTS_EXAMPLE),
			@ExampleObject(name = "Sah (OUTCOME)", value = OUTCOME_EXAMPLE)}))
	@ApiResponse(responseCode = "200", description = "Meč je izmenjen")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije učesnik meča ni GROUP_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Meč ili sport ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Druga grupa, rezultat ne odgovara pravilima sporta, igrač nije član grupe ili je deaktiviran, ili je sport obrisan",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public MatchResponse update(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @PathVariable final String id,
	                            @RequestBody @Valid final MatchRequest request)
	{
		return _matchMapper.toResponse(
				_matchService.update(id, _matchMapper.toModel(request), principal.getUser().getId()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Brisanje meča (učesnik meča ili GROUP_ADMIN)",
			description = "Učesnik može da obriše meč i ako je u međuvremenu napustio grupu.")
	@ApiResponse(responseCode = "204", description = "Meč je obrisan")
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije učesnik meča ni GROUP_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Meč ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void delete(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_matchService.delete(id, principal.getUser().getId());
	}
}
