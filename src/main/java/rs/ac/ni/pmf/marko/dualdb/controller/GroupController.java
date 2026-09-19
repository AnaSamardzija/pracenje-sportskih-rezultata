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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.marko.dualdb.dto.group.GroupRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.group.GroupResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.GroupMapper;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.marko.dualdb.service.GroupService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController
{
	private final GroupService _groupService;
	private final GroupMapper _groupMapper;

	@GetMapping
	public List<GroupResponse> getAll(@RequestParam(required = false) final String search)
	{
		return _groupService.findAll(search).stream()
				.map(_groupMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public GroupResponse getById(@PathVariable final String id)
	{
		return _groupMapper.toResponse(_groupService.findById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public GroupResponse create(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @RequestBody @Valid final GroupRequest request)
	{
		return _groupMapper.toResponse(_groupService.create(_groupMapper.toModel(request), principal.getUser().getId()));
	}

	@PutMapping("/{id}")
	public GroupResponse update(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @PathVariable final String id,
	                            @RequestBody @Valid final GroupRequest request)
	{
		return _groupMapper.toResponse(_groupService.update(id, _groupMapper.toModel(request), principal.getUser().getId()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_groupService.delete(id, principal.getUser().getId());
	}
}
