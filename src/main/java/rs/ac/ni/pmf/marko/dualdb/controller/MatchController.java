package rs.ac.ni.pmf.marko.dualdb.controller;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
public class MatchController
{
	private final MatchService _matchService;
	private final MatchMapper _matchMapper;

	@GetMapping
	public List<MatchResponse> getAll()
	{
		return _matchService.findAll().stream()
				.map(_matchMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public MatchResponse getById(@PathVariable final String id)
	{
		return _matchMapper.toResponse(_matchService.findById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MatchResponse create(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @RequestBody @Valid final MatchRequest request)
	{
		return _matchMapper.toResponse(_matchService.create(_matchMapper.toModel(request), principal.getUser().getId()));
	}

	@PutMapping("/{id}")
	public MatchResponse update(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @PathVariable final String id,
	                            @RequestBody @Valid final MatchRequest request)
	{
		return _matchMapper.toResponse(
				_matchService.update(id, _matchMapper.toModel(request), principal.getUser().getId()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_matchService.delete(id, principal.getUser().getId());
	}
}
