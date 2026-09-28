package rs.ac.ni.pmf.ana.dualdb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportResponse;
import rs.ac.ni.pmf.ana.dualdb.dto.mapper.SportMapper;
import rs.ac.ni.pmf.ana.dualdb.service.SportService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sports")
@RequiredArgsConstructor
@Tag(name = "Sportovi")
public class SportController
{
	private static final String TENIS_EXAMPLE = """
			{
			  "name": "Tenis",
			  "type": "INDIVIDUAL",
			  "scoringMode": "SETS",
			  "rules": {
			    "allowDraw": false,
			    "minPlayersPerSide": 1,
			    "maxPlayersPerSide": 1,
			    "bestOf": 3,
			    "pointsToWinSet": 6,
			    "pointsForWin": 3,
			    "pointsForDraw": 1,
			    "pointsForLoss": 0
			  }
			}""";

	private static final String FUDBAL_EXAMPLE = """
			{
			  "name": "Fudbal",
			  "type": "TEAM",
			  "scoringMode": "POINTS",
			  "rules": {
			    "allowDraw": true,
			    "minPlayersPerSide": 1,
			    "maxPlayersPerSide": 11,
			    "pointsForWin": 3,
			    "pointsForDraw": 1,
			    "pointsForLoss": 0
			  }
			}""";

	private static final String SAH_EXAMPLE = """
			{
			  "name": "Sah",
			  "type": "INDIVIDUAL",
			  "scoringMode": "OUTCOME",
			  "rules": {
			    "allowDraw": true,
			    "minPlayersPerSide": 1,
			    "maxPlayersPerSide": 1,
			    "pointsForWin": 2,
			    "pointsForDraw": 1,
			    "pointsForLoss": 0
			  }
			}""";

	private final SportService _sportService;
	private final SportMapper _sportMapper;

	@GetMapping
	@PreAuthorize("!#includeInactive or hasRole('SYSTEM_ADMIN')")
	@Operation(summary = "Spisak aktivnih sportova (prijavljen korisnik)",
			description = "Sa ?includeInactive=true vraća i obrisane (neaktivne) sportove; to sme samo SYSTEM_ADMIN.")
	@ApiResponse(responseCode = "200", description = "Spisak sportova")
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "includeInactive=true, a korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public List<SportResponse> getAll(@RequestParam(required = false, defaultValue = "false") final boolean includeInactive)
	{
		return _sportService.findAll(includeInactive).stream()
				.map(_sportMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@PreAuthorize("!#includeInactive or hasRole('SYSTEM_ADMIN')")
	@Operation(summary = "Sport po id-u (prijavljen korisnik)",
			description = "Obrisan (neaktivan) sport vidi samo SYSTEM_ADMIN, sa ?includeInactive=true.")
	@ApiResponse(responseCode = "200", description = "Sport")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "includeInactive=true, a korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Sport ne postoji ili je obrisan",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public SportResponse getById(@PathVariable final String id,
	                             @RequestParam(required = false, defaultValue = "false") final boolean includeInactive)
	{
		return _sportMapper.toResponse(_sportService.findById(id, includeInactive));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Operation(summary = "Kreiranje sporta (samo SYSTEM_ADMIN)",
			description = "Ime sporta je jedinstveno bez obzira na velika i mala slova, i među obrisanim sportovima.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
			@ExampleObject(name = "Tenis (SETS)", value = TENIS_EXAMPLE),
			@ExampleObject(name = "Fudbal (POINTS)", value = FUDBAL_EXAMPLE),
			@ExampleObject(name = "Sah (OUTCOME)", value = SAH_EXAMPLE)}))
	@ApiResponse(responseCode = "201", description = "Sport je kreiran")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "Sport sa tim imenom već postoji (aktivan ili obrisan)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "maxPlayersPerSide je manji od minPlayersPerSide ili INDIVIDUAL sport nema tačno 1 igrača po strani",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public SportResponse create(@RequestBody @Valid final SportRequest request)
	{
		return _sportMapper.toResponse(_sportService.create(_sportMapper.toModel(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Operation(summary = "Izmena sporta (samo SYSTEM_ADMIN)",
			description = "Telo i validacija su isti kao kod kreiranja; sport može da zadrži svoje ime.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
			@ExampleObject(name = "Tenis (SETS)", value = TENIS_EXAMPLE),
			@ExampleObject(name = "Fudbal (POINTS)", value = FUDBAL_EXAMPLE),
			@ExampleObject(name = "Sah (OUTCOME)", value = SAH_EXAMPLE)}))
	@ApiResponse(responseCode = "200", description = "Sport je izmenjen")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva ili nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Sport ne postoji ili je obrisan",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "Ime pripada drugom sportu",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "maxPlayersPerSide je manji od minPlayersPerSide ili INDIVIDUAL sport nema tačno 1 igrača po strani",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public SportResponse update(@PathVariable final String id, @RequestBody @Valid final SportRequest request)
	{
		return _sportMapper.toResponse(_sportService.update(id, _sportMapper.toModel(request)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Operation(summary = "Brisanje sporta (samo SYSTEM_ADMIN)",
			description = "Sport se ne briše iz baze, već postaje neaktivan i nestaje iz spiska. Njegovi mečevi ostaju "
					+ "i računaju se u rang-listama, ali novi ne mogu da se unose. Vraća se preko PATCH /restore.")
	@ApiResponse(responseCode = "204", description = "Sport je obrisan (neaktivan)")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Sport ne postoji ili je već obrisan",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void delete(@PathVariable final String id)
	{
		_sportService.delete(id);
	}

	@PatchMapping("/{id}/restore")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	@Operation(summary = "Vraćanje obrisanog sporta (samo SYSTEM_ADMIN)")
	@ApiResponse(responseCode = "200", description = "Sport je ponovo aktivan")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije SYSTEM_ADMIN",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Sport ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Sport je već aktivan",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public SportResponse restore(@PathVariable final String id)
	{
		return _sportMapper.toResponse(_sportService.restore(id));
	}
}
