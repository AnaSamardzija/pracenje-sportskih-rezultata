package rs.ac.ni.pmf.marko.dualdb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	public List<SportResponse> getAll()
	{
		return _sportService.findAll().stream()
				.map(_sportMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public SportResponse getById(@PathVariable final String id)
	{
		return _sportMapper.toResponse(_sportService.findById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public SportResponse create(@RequestBody @Valid final SportRequest request)
	{
		return _sportMapper.toResponse(_sportService.create(_sportMapper.toModel(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public SportResponse update(@PathVariable final String id, @RequestBody @Valid final SportRequest request)
	{
		return _sportMapper.toResponse(_sportService.update(id, _sportMapper.toModel(request)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(@PathVariable final String id)
	{
		_sportService.delete(id);
	}
}
