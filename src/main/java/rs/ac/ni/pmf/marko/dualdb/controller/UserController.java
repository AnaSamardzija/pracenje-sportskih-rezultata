package rs.ac.ni.pmf.marko.dualdb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.RankingMapper;
import rs.ac.ni.pmf.marko.dualdb.dto.ranking.PlayerStatsResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.user.ChangePasswordRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.user.UpdateProfileRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.user.UserDto;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.UserMapper;
import rs.ac.ni.pmf.marko.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.marko.dualdb.service.RankingService;
import rs.ac.ni.pmf.marko.dualdb.service.UserService;

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
	public UserDto getCurrentUser(@AuthenticationPrincipal final CustomUserDetails principal)
	{
		return _userMapper.toDto(principal.getUser());
	}

	@PutMapping("/me")
	public UserDto updateCurrentUser(@AuthenticationPrincipal final CustomUserDetails principal,
	                                 @RequestBody @Valid final UpdateProfileRequest request)
	{
		return _userMapper.toDto(_userService.updateProfile(principal.getUsername(), request));
	}

	@PutMapping("/me/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@AuthenticationPrincipal final CustomUserDetails principal,
	                           @RequestBody @Valid final ChangePasswordRequest request)
	{
		_userService.changePassword(principal.getUsername(), request);
	}

	@GetMapping("/me/stats")
	public PlayerStatsResponse getCurrentUserStats(@AuthenticationPrincipal final CustomUserDetails principal,
	                                               @RequestParam(required = false) final String sportId)
	{
		return _rankingMapper.toResponse(_rankingService.playerStats(principal.getUser().getId(), sportId));
	}

	@GetMapping("/{id}")
	public UserDto getUserById(@PathVariable final String id)
	{
		return _userMapper.toDto(_userService.findById(id));
	}

	@GetMapping("/{id}/stats")
	public PlayerStatsResponse getUserStats(@PathVariable final String id,
	                                        @RequestParam(required = false) final String sportId)
	{
		return _rankingMapper.toResponse(_rankingService.playerStats(id, sportId));
	}

	@GetMapping
	public List<UserDto> getAllUsers()
	{
		return _userService.findAll().stream()
				.map(_userMapper::toDto)
				.collect(Collectors.toList());
	}
}
