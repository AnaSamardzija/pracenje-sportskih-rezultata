package rs.ac.ni.pmf.marko.dualdb.controller;

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
import rs.ac.ni.pmf.marko.dualdb.dto.sport.SportRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.sport.SportResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.SportMapper;
import rs.ac.ni.pmf.marko.dualdb.service.SportService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sports")
@RequiredArgsConstructor
public class SportController
{
	private final SportService _sportService;
	private final SportMapper _sportMapper;

	@GetMapping
	@PreAuthorize("!#includeInactive or hasRole('SYSTEM_ADMIN')")
	public List<SportResponse> getAll(@RequestParam(required = false, defaultValue = "false") final boolean includeInactive)
	{
		return _sportService.findAll(includeInactive).stream()
				.map(_sportMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@PreAuthorize("!#includeInactive or hasRole('SYSTEM_ADMIN')")
	public SportResponse getById(@PathVariable final String id,
	                             @RequestParam(required = false, defaultValue = "false") final boolean includeInactive)
	{
		return _sportMapper.toResponse(_sportService.findById(id, includeInactive));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public SportResponse create(@RequestBody @Valid final SportRequest request)
	{
		return _sportMapper.toResponse(_sportService.create(_sportMapper.toModel(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public SportResponse update(@PathVariable final String id, @RequestBody @Valid final SportRequest request)
	{
		return _sportMapper.toResponse(_sportService.update(id, _sportMapper.toModel(request)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public void delete(@PathVariable final String id)
	{
		_sportService.delete(id);
	}

	@PatchMapping("/{id}/restore")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public SportResponse restore(@PathVariable final String id)
	{
		return _sportMapper.toResponse(_sportService.restore(id));
	}
}
