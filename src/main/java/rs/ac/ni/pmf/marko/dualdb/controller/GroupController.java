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
import rs.ac.ni.pmf.marko.dualdb.dto.group.MemberResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.GroupMapper;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.marko.dualdb.service.GroupService;
import rs.ac.ni.pmf.marko.dualdb.service.MembershipService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController
{
	private final GroupService _groupService;
	private final MembershipService _membershipService;
	private final GroupMapper _groupMapper;

	@GetMapping
	public List<GroupResponse> getAll(@AuthenticationPrincipal final CustomUserDetails principal,
	                                  @RequestParam(required = false, defaultValue = "false") final boolean mine,
	                                  @RequestParam(required = false) final String search)
	{
		return _groupService.findAll(mine, search, principal.getUser().getId()).stream()
				.map(_groupMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public GroupResponse getById(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		return _groupMapper.toResponse(_groupService.findById(id, principal.getUser().getId()));
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

	@GetMapping("/{id}/members")
	public List<MemberResponse> getMembers(@PathVariable final String id)
	{
		return _membershipService.listMembers(id).stream()
				.map(_groupMapper::toMemberResponse)
				.collect(Collectors.toList());
	}

	@PostMapping("/{id}/members")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void join(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_membershipService.join(id, principal.getUser().getId());
	}

	@DeleteMapping("/{id}/members/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void leave(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_membershipService.leave(id, principal.getUser().getId());
	}

	@DeleteMapping("/{id}/members/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void kick(@AuthenticationPrincipal final CustomUserDetails principal,
	                 @PathVariable final String id,
	                 @PathVariable final String userId)
	{
		_membershipService.kick(id, userId, principal.getUser().getId());
	}
}
