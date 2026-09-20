package rs.ac.ni.pmf.marko.dualdb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.RankingMapper;
import rs.ac.ni.pmf.marko.dualdb.dto.ranking.RankingEntryResponse;
import rs.ac.ni.pmf.marko.dualdb.service.RankingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController
{
	private final RankingService _rankingService;
	private final RankingMapper _rankingMapper;

	@GetMapping
	public List<RankingEntryResponse> getRanking(@RequestParam(required = false) final String groupId,
	                                             @RequestParam(required = false) final String sportId)
	{
		return _rankingService.ranking(groupId, sportId).stream()
				.map(_rankingMapper::toResponse)
				.collect(Collectors.toList());
	}
}
