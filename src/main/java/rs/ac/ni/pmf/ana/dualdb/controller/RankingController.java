package rs.ac.ni.pmf.ana.dualdb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.ana.dualdb.dto.mapper.RankingMapper;
import rs.ac.ni.pmf.ana.dualdb.dto.ranking.RankingEntryResponse;
import rs.ac.ni.pmf.ana.dualdb.service.RankingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
@Tag(name = "Rang-liste i statistika")
public class RankingController
{
	private final RankingService _rankingService;
	private final RankingMapper _rankingMapper;

	@GetMapping
	@Operation(summary = "Rang-lista (prijavljen korisnik)",
			description = "?groupId= i ?sportId= nisu obavezni i kombinuju se; bez njih se računaju svi mečevi. Sortirano po bodovima, "
					+ "pa pobedama, pa korisničkom imenu; izjednačeni dele mesto, a sledeće se preskače (1, 1, 3). "
					+ "Bodovi se računaju po meču iz pravila tog sporta, a mečevi obrisanog sporta se i dalje računaju. "
					+ "Računa se u aplikaciji iz mečeva, a ne iz tabele player_stats.")
	@ApiResponse(responseCode = "200", description = "Rang-lista")
	@ApiResponse(responseCode = "400", description = "Nenumerički id u filteru (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ili sport ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public List<RankingEntryResponse> getRanking(@RequestParam(required = false) final String groupId,
	                                             @RequestParam(required = false) final String sportId)
	{
		return _rankingService.ranking(groupId, sportId).stream()
				.map(_rankingMapper::toResponse)
				.toList();
	}
}
