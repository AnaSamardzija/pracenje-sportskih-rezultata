package rs.ac.ni.pmf.marko.dualdb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.marko.dualdb.dto.user.UserDto;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.UserMapper;
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

	@GetMapping("/{id}")
	public UserDto getUserById(@PathVariable final String id)
	{
		return _userMapper.toDto(_userService.findById(id));
	}

	@GetMapping
	public List<UserDto> getAllUsers()
	{
		return _userService.findAll().stream()
				.map(_userMapper::toDto)
				.collect(Collectors.toList());
	}
}
