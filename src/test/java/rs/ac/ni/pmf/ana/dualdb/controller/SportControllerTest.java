package rs.ac.ni.pmf.ana.dualdb.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.ni.pmf.ana.dualdb.TestData;
import rs.ac.ni.pmf.ana.dualdb.dto.mapper.SportMapper;
import rs.ac.ni.pmf.ana.dualdb.dto.sport.SportRequest;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.security.JwtUtil;
import rs.ac.ni.pmf.ana.dualdb.security.SecurityConfig;
import rs.ac.ni.pmf.ana.dualdb.security.StorageSelectionUserDetailsService;
import rs.ac.ni.pmf.ana.dualdb.service.SportService;
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
				TestData.SPORTS.tennis(),
				TestData.SPORTS.chess()
		));

		_mockMvc.perform(get("/api/v1/sports"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].name").value("Tennis"))
				.andExpect(jsonPath("$[1].name").value("Chess"));
	}

	@Test
	@WithMockUser
	void shouldReturnSportById() throws Exception
	{
		when(_sportService.findById(TestData.SPORTS.TENNIS_ID, false)).thenReturn(TestData.SPORTS.tennis());

		_mockMvc.perform(get("/api/v1/sports/" + TestData.SPORTS.TENNIS_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.TENNIS_ID))
				.andExpect(jsonPath("$.name").value("Tennis"))
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
				.andExpect(jsonPath("$.message").value("Sport with id 999 not found"))
				.andExpect(jsonPath("$.path").value("/api/v1/sports/999"));
	}

	@Test
	@WithMockUser(authorities = "sports.create")
	void shouldCreateSport() throws Exception
	{
		when(_sportService.create(TestData.SPORTS.newTennis())).thenReturn(TestData.SPORTS.tennis());

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tennisRequest())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.TENNIS_ID))
				.andExpect(jsonPath("$.name").value("Tennis"))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	@WithMockUser(authorities = "sports.create")
	void shouldReturn400WhenRequestIsInvalid() throws Exception
	{
		final SportRequest tennis = TestData.SPORTS.tennisRequest();
		final SportRequest request = SportRequest.builder()
				.name("")
				.type(tennis.getType())
				.scoringMode(tennis.getScoringMode())
				.rules(tennis.getRules())
				.build();

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("name: name is required"));

		verify(_sportService, never()).create(any());
	}

	@Test
	@WithMockUser(authorities = "sports.create")
	void shouldReturn409WhenSportNameIsTaken() throws Exception
	{
		when(_sportService.create(any(Sport.class)))
				.thenThrow(new DuplicateResourceException("Sport with name Tennis already exists"));

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tennisRequest())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Sport with name Tennis already exists"));
	}

	@Test
	@WithMockUser(authorities = "sports.create")
	void shouldReturn422WhenMaxPlayersIsLessThanMin() throws Exception
	{
		when(_sportService.create(any(Sport.class)))
				.thenThrow(new InvalidOperationException("maxPlayersPerSide cannot be less than minPlayersPerSide"));

		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tennisRequest())))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.message").value("maxPlayersPerSide cannot be less than minPlayersPerSide"));
	}

	@Test
	@WithMockUser(authorities = "sports.update")
	void shouldUpdateSport() throws Exception
	{
		when(_sportService.update(TestData.SPORTS.TENNIS_ID, TestData.SPORTS.newTennis()))
				.thenReturn(TestData.SPORTS.tennis());

		_mockMvc.perform(put("/api/v1/sports/" + TestData.SPORTS.TENNIS_ID)
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tennisRequest())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.TENNIS_ID))
				.andExpect(jsonPath("$.name").value("Tennis"));
	}

	@Test
	@WithMockUser(authorities = "sports.delete")
	void shouldDeleteSport() throws Exception
	{
		_mockMvc.perform(delete("/api/v1/sports/" + TestData.SPORTS.TENNIS_ID))
				.andExpect(status().isNoContent());

		verify(_sportService).delete(TestData.SPORTS.TENNIS_ID);
	}

	@Test
	@WithMockUser(authorities = "sports.restore")
	void shouldRestoreSport() throws Exception
	{
		when(_sportService.restore(TestData.SPORTS.CHESS_ID)).thenReturn(TestData.SPORTS.chess());

		_mockMvc.perform(patch("/api/v1/sports/" + TestData.SPORTS.CHESS_ID + "/restore"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TestData.SPORTS.CHESS_ID))
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
	@WithMockUser(roles = "SYSTEM_ADMIN")
	void shouldReturn403WithoutSportsCreatePermission() throws Exception
	{
		_mockMvc.perform(post("/api/v1/sports")
						         .contentType(MediaType.APPLICATION_JSON)
						         .content(_jsonMapper.writeValueAsString(TestData.SPORTS.tennisRequest())))
				.andExpect(status().isForbidden());

		verify(_sportService, never()).create(any());
	}
}
