package rs.ac.ni.pmf.marko.dualdb.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.ni.pmf.marko.dualdb.TestData;
import rs.ac.ni.pmf.marko.dualdb.dto.mapper.SportMapper;
import rs.ac.ni.pmf.marko.dualdb.dto.sport.SportRequest;
import rs.ac.ni.pmf.marko.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.marko.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.security.JwtUtil;
import rs.ac.ni.pmf.marko.dualdb.security.SecurityConfig;
import rs.ac.ni.pmf.marko.dualdb.security.StorageSelectionUserDetailsService;
import rs.ac.ni.pmf.marko.dualdb.service.SportService;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SportController.class)
@Import({SecurityConfig.class, SportMapper.class})
class SportControllerTest
{
	@Autowired
	MockMvc _mockMvc;

	@Autowired
	JsonMapper _jsonMapper;

	@MockitoBean
	SportService _sportService;

	@MockitoBean
	JwtUtil _jwtUtil;

	@MockitoBean
	StorageSelectionUserDetailsService _userDetailsService;

	@Test
	@WithMockUser
	void shouldReturnAllSports() throws Exception
	{
		when(_sportService.findAll(false)).thenReturn(List.of(
				TestData.SPORTS.tenis(),
				TestData.SPORTS.sah()
		));

		_mockMvc.perform(get("/api/v1/sports"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].name").value("Tenis"))
				.andExpect(jsonPath("$[1].name").value("Sah"));
	}

	@Test
	@WithMockUser
	void shouldReturnSportById() throws Exception
	{
		when(_sportService.findById(TestData.SPORTS.TENIS_ID, false)).thenReturn(TestData.SPORTS.tenis());

		_mockMvc.perform(get("/api/v1/sports/" + TestData.SPORTS.TENIS_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.TENIS_ID))
				.andExpect(jsonPath("$.name").value("Tenis"))
				.andExpect(jsonPath("$.scoringMode").value("SETS"))
				.andExpect(jsonPath("$.rules.bestOf").value(3))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	@WithMockUser
	void shouldReturn404WhenSportNotFound() throws Exception
	{
		when(_sportService.findById("999", false))
				.thenThrow(new ResourceNotFoundException("Sport with id 999 not found"));

		_mockMvc.perform(get("/api/v1/sports/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Sport with id 999 not found"));
	}

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldCreateSport() throws Exception
	{
		when(_sportService.create(TestData.SPORTS.newTenis())).thenReturn(TestData.SPORTS.tenis());

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tenisRequest())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.TENIS_ID))
				.andExpect(jsonPath("$.name").value("Tenis"))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldReturn400WhenRequestIsInvalid() throws Exception
	{
		final SportRequest tenis = TestData.SPORTS.tenisRequest();
		final SportRequest request = SportRequest.builder()
				.name("")
				.type(tenis.getType())
				.scoringMode(tenis.getScoringMode())
				.rules(tenis.getRules())
				.build();

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("name: name is required"));

		verify(_sportService, never()).create(any());
	}

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldReturn409WhenSportNameIsTaken() throws Exception
	{
		when(_sportService.create(any(Sport.class)))
				.thenThrow(new DuplicateResourceException("Sport with name Tenis already exists"));

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tenisRequest())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Sport with name Tenis already exists"));
	}

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldReturn422WhenMaxPlayersIsLessThanMin() throws Exception
	{
		when(_sportService.create(any(Sport.class)))
				.thenThrow(new InvalidOperationException("maxPlayersPerSide cannot be less than minPlayersPerSide"));

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tenisRequest())))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.message").value("maxPlayersPerSide cannot be less than minPlayersPerSide"));
	}

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldUpdateSport() throws Exception
	{
		when(_sportService.update(TestData.SPORTS.TENIS_ID, TestData.SPORTS.newTenis()))
				.thenReturn(TestData.SPORTS.tenis());

		_mockMvc.perform(put("/api/v1/sports/" + TestData.SPORTS.TENIS_ID)
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tenisRequest())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.TENIS_ID))
				.andExpect(jsonPath("$.name").value("Tenis"));
	}

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldDeleteSport() throws Exception
	{
		_mockMvc.perform(delete("/api/v1/sports/" + TestData.SPORTS.TENIS_ID))
				.andExpect(status().isNoContent());

		verify(_sportService).delete(TestData.SPORTS.TENIS_ID);
	}

	@Test
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldRestoreSport() throws Exception
	{
		when(_sportService.restore(TestData.SPORTS.SAH_ID)).thenReturn(TestData.SPORTS.sah());

		_mockMvc.perform(patch("/api/v1/sports/" + TestData.SPORTS.SAH_ID + "/restore"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.SAH_ID))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void shouldReturn401WhenNotLoggedIn() throws Exception
	{
		_mockMvc.perform(get("/api/v1/sports"))
				.andExpect(status().isUnauthorized());

		verify(_sportService, never()).findAll(anyBoolean());
	}

	@Test
	@WithMockUser
	void shouldReturn403WhenUserIsNotSystemAdmin() throws Exception
	{
		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tenisRequest())))
				.andExpect(status().isForbidden());

		verify(_sportService, never()).create(any());
	}
}
