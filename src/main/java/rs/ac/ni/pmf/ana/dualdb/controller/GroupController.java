package rs.ac.ni.pmf.ana.dualdb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import rs.ac.ni.pmf.ana.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.ana.dualdb.dto.group.AddMemberRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.group.GroupRequest;
import rs.ac.ni.pmf.ana.dualdb.dto.group.GroupResponse;
import rs.ac.ni.pmf.ana.dualdb.dto.group.MemberResponse;
import rs.ac.ni.pmf.ana.dualdb.dto.mapper.GroupMapper;
import rs.ac.ni.pmf.ana.dualdb.security.CustomUserDetails;
import rs.ac.ni.pmf.ana.dualdb.service.GroupService;
import rs.ac.ni.pmf.ana.dualdb.service.MembershipService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Grupe i članstvo")
public class GroupController
{
	private final GroupService _groupService;
	private final MembershipService _membershipService;
	private final GroupMapper _groupMapper;

	@GetMapping
	@Operation(summary = "Spisak grupa (prijavljen korisnik)",
			description = "?search= traži po delu imena, bez obzira na velika i mala slova; ?mine=true vraća samo grupe u kojima sam član. "
					+ "myRole je GROUP_ADMIN, MEMBER ili null ako nisam član.")
	@ApiResponse(responseCode = "200", description = "Spisak grupa")
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public List<GroupResponse> getAll(@AuthenticationPrincipal final CustomUserDetails principal,
	                                  @RequestParam(required = false, defaultValue = "false") final boolean mine,
	                                  @RequestParam(required = false) final String search)
	{
		return _groupService.findAll(mine, search, principal.getUser().getId()).stream()
				.map(_groupMapper::toResponse)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Grupa po id-u (prijavljen korisnik)",
			description = "Vidi je i onaj ko nije član, tada je myRole null.")
	@ApiResponse(responseCode = "200", description = "Grupa")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public GroupResponse getById(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		return _groupMapper.toResponse(_groupService.findById(id, principal.getUser().getId()));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Kreiranje grupe (prijavljen korisnik)",
			description = "Onaj ko pravi grupu postaje njen GROUP_ADMIN. description nije obavezan, a dve grupe mogu imati isto ime.")
	@ApiResponse(responseCode = "201", description = "Grupa je kreirana")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public GroupResponse create(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @RequestBody @Valid final GroupRequest request)
	{
		return _groupMapper.toResponse(_groupService.create(_groupMapper.toModel(request), principal.getUser().getId()));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Izmena grupe (GROUP_ADMIN ili permisija groups.update_any)",
			description = "Sa permisijom groups.update_any (ima je SYSTEM_ADMIN) menja se i grupa čiji korisnik nije član.")
	@ApiResponse(responseCode = "200", description = "Grupa je izmenjena")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva ili nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije GROUP_ADMIN te grupe i nema permisiju groups.update_any",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public GroupResponse update(@AuthenticationPrincipal final CustomUserDetails principal,
	                            @PathVariable final String id,
	                            @RequestBody @Valid final GroupRequest request)
	{
		return _groupMapper.toResponse(_groupService.update(id, _groupMapper.toModel(request), principal.getUser()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Brisanje grupe (GROUP_ADMIN ili permisija groups.delete_any)",
			description = "Brišu se i sva članstva. Grupa koja ima unete mečeve ne može da se obriše.")
	@ApiResponse(responseCode = "204", description = "Grupa je obrisana")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije GROUP_ADMIN te grupe i nema permisiju groups.delete_any",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Grupa ima unete mečeve",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void delete(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_groupService.delete(id, principal.getUser());
	}

	@GetMapping("/{id}/members")
	@Operation(summary = "Lista članova grupe (prijavljen korisnik)",
			description = "Deaktivirani članovi ostaju na listi sa active=false; ne mogu da budu igrači u novom meču.")
	@ApiResponse(responseCode = "200", description = "Članovi grupe sa ulogom u grupi")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public List<MemberResponse> getMembers(@PathVariable final String id)
	{
		return _membershipService.listMembers(id).stream()
				.map(_groupMapper::toMemberResponse)
				.collect(Collectors.toList());
	}

	@PostMapping("/{id}/members")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Dodavanje člana u grupu (GROUP_ADMIN ili permisija groups.members.add_any)",
			description = "Korisnik se traži po username-u, bez obzira na velika i mala slova, i postaje MEMBER grupe, "
					+ "a GROUP_ADMIN ako grupa nema aktivnog admina.")
	@ApiResponse(responseCode = "201", description = "Korisnik je dodat u grupu")
	@ApiResponse(responseCode = "400", description = "Neispravno telo zahteva ili nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije GROUP_ADMIN te grupe i nema permisiju groups.members.add_any",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ili korisnik ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "Korisnik je već član grupe",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Korisnik je deaktiviran",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public MemberResponse addMember(@AuthenticationPrincipal final CustomUserDetails principal,
	                                @PathVariable final String id,
	                                @RequestBody @Valid final AddMemberRequest request)
	{
		return _groupMapper.toMemberResponse(
				_membershipService.addMember(id, request.getUsername(), principal.getUser()));
	}

	@PostMapping("/{id}/members/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Pridruživanje grupi (prijavljen korisnik)",
			description = "Bez tela; korisnik postaje MEMBER grupe, a GROUP_ADMIN ako grupa nema aktivnog admina.")
	@ApiResponse(responseCode = "204", description = "Korisnik je postao član")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "Korisnik je već član grupe",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void join(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_membershipService.join(id, principal.getUser().getId());
	}

	@DeleteMapping("/{id}/members/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Napuštanje grupe (član grupe)",
			description = "Kad ode poslednji GROUP_ADMIN, admin postaje aktivan član koji je najduže u grupi. GROUP_ADMIN koji je jedini "
					+ "aktivan član ne može da ode, već briše grupu. Mečevi tog igrača ostaju.")
	@ApiResponse(responseCode = "204", description = "Korisnik je napustio grupu")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Korisnik nije član grupe ili grupa ne postoji",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Korisnik je GROUP_ADMIN i jedini aktivan član grupe",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void leave(@AuthenticationPrincipal final CustomUserDetails principal, @PathVariable final String id)
	{
		_membershipService.leave(id, principal.getUser().getId());
	}

	@DeleteMapping("/{id}/members/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Izbacivanje člana iz grupe (GROUP_ADMIN ili permisija groups.members.kick_any)",
			description = "Admin ne može da izbaci sebe, za to služi napuštanje grupe. Kad korisnik sa permisijom groups.members.kick_any izbaci jedinog admina grupe, "
					+ "admin postaje aktivan član koji je najduže u grupi.")
	@ApiResponse(responseCode = "204", description = "Član je izbačen")
	@ApiResponse(responseCode = "400", description = "Nenumerički id (samo MariaDB)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "Token nije poslat, neispravan je ili je istekao",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "Korisnik nije GROUP_ADMIN te grupe i nema permisiju groups.members.kick_any",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "Grupa ne postoji ili korisnik nije njen član",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "422", description = "Admin pokušava da izbaci sebe",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	public void kick(@AuthenticationPrincipal final CustomUserDetails principal,
	                 @PathVariable final String id,
	                 @PathVariable final String userId)
	{
		_membershipService.kick(id, userId, principal.getUser());
	}
}
