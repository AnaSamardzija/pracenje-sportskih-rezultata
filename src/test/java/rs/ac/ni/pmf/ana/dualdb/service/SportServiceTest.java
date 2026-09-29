package rs.ac.ni.pmf.ana.dualdb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.ni.pmf.ana.dualdb.TestData;
import rs.ac.ni.pmf.ana.dualdb.data.StorageType;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.ana.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.ana.dualdb.storage.sport.SportStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SportServiceTest
{
	@Mock
	StorageResolver _storageResolver;

	@Mock
	CurrentStorageTypeProvider _storageTypeProvider;

	@Mock
	SportStorage _sportStorage;

	@Mock
	MatchService _matchService;

	@InjectMocks
	SportService _sportService;

	// Servis ne zna za konkretnu bazu, pa je svejedno koja se ovde izabere: resolver uvek vraća mock storage.
	@BeforeEach
	void setUp()
	{
		when(_storageTypeProvider.getCurrentStorageType()).thenReturn(StorageType.MARIADB);
		when(_storageResolver.resolve(StorageType.MARIADB, Sport.class)).thenReturn(Optional.of(_sportStorage));
	}

	@Test
	void shouldReturnOnlyActiveSports()
	{
		final Sport tennis = TestData.SPORTS.tennis();
		when(_sportStorage.findAllActive()).thenReturn(List.of(tennis));

		assertThat(_sportService.findAll(false)).containsExactly(tennis);
	}

	@Test
	void shouldReturnAllSportsWhenIncludeInactive()
	{
		final Sport tennis = TestData.SPORTS.tennis();
		final Sport chess = TestData.SPORTS.chess();
		chess.setActive(false);
		when(_sportStorage.findAll()).thenReturn(List.of(tennis, chess));

		assertThat(_sportService.findAll(true)).containsExactly(tennis, chess);
	}

	@Test
	void shouldReturnSportById()
	{
		final Sport tennis = TestData.SPORTS.tennis();
		when(_sportStorage.findById(TestData.SPORTS.TENNIS_ID)).thenReturn(Optional.of(tennis));

		assertThat(_sportService.findById(TestData.SPORTS.TENNIS_ID, false)).isEqualTo(tennis);
	}

	@Test
	void shouldThrowNotFoundWhenSportDoesNotExist()
	{
		when(_sportStorage.findById("999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> _sportService.findById("999", false))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Sport with id 999 not found");
	}

	@Test
	void shouldThrowNotFoundWhenSportIsInactive()
	{
		final Sport chess = TestData.SPORTS.chess();
		chess.setActive(false);
		when(_sportStorage.findById(TestData.SPORTS.CHESS_ID)).thenReturn(Optional.of(chess));

		assertThatThrownBy(() -> _sportService.findById(TestData.SPORTS.CHESS_ID, false))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void shouldReturnInactiveSportWhenIncludeInactive()
	{
		final Sport chess = TestData.SPORTS.chess();
		chess.setActive(false);
		when(_sportStorage.findById(TestData.SPORTS.CHESS_ID)).thenReturn(Optional.of(chess));

		assertThat(_sportService.findById(TestData.SPORTS.CHESS_ID, true)).isEqualTo(chess);
	}

	@Test
	void shouldCreateSportAsActive()
	{
		final Sport input = TestData.SPORTS.newTennis();
		when(_sportStorage.findByName("Tennis")).thenReturn(Optional.empty());
		when(_sportStorage.save(input)).thenReturn(input);

		final Sport created = _sportService.create(input);

		assertThat(created.isActive()).isTrue();
		verify(_sportStorage).save(input);
	}

	@Test
	void shouldThrowDuplicateWhenNameIsTaken()
	{
		when(_sportStorage.findByName("Tennis")).thenReturn(Optional.of(TestData.SPORTS.tennis()));

		assertThatThrownBy(() -> _sportService.create(TestData.SPORTS.newTennis()))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Sport with name Tennis already exists");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldThrowDuplicateWithRestoreHintWhenNameBelongsToInactiveSport()
	{
		final Sport inactiveTennis = TestData.SPORTS.tennis();
		inactiveTennis.setActive(false);
		when(_sportStorage.findByName("Tennis")).thenReturn(Optional.of(inactiveTennis));

		assertThatThrownBy(() -> _sportService.create(TestData.SPORTS.newTennis()))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Sport with name Tennis already exists but is inactive; restore it instead");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldThrowInvalidOperationWhenMaxPlayersIsLessThanMin()
	{
		final Sport input = TestData.SPORTS.newFootball();
		input.getRules().setMinPlayersPerSide(12);
		when(_sportStorage.findByName("Football")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> _sportService.create(input))
				.isInstanceOf(InvalidOperationException.class)
				.hasMessage("maxPlayersPerSide cannot be less than minPlayersPerSide");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldThrowInvalidOperationWhenIndividualSportAllowsMoreThanOnePlayer()
	{
		final Sport input = TestData.SPORTS.newTennis();
		input.getRules().setMaxPlayersPerSide(2);
		when(_sportStorage.findByName("Tennis")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> _sportService.create(input))
				.isInstanceOf(InvalidOperationException.class)
				.hasMessage("An individual sport must have exactly 1 player per side");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldUpdateSport()
	{
		final Sport existing = TestData.SPORTS.tennis();
		final Sport input = TestData.SPORTS.newFootball();
		when(_sportStorage.findById(TestData.SPORTS.TENNIS_ID)).thenReturn(Optional.of(existing));
		when(_sportStorage.findByName("Football")).thenReturn(Optional.empty());
		when(_sportStorage.save(existing)).thenReturn(existing);

		final Sport updated = _sportService.update(TestData.SPORTS.TENNIS_ID, input);

		assertThat(updated.getId()).isEqualTo(TestData.SPORTS.TENNIS_ID);
		assertThat(updated.getName()).isEqualTo(input.getName());
		assertThat(updated.getType()).isEqualTo(input.getType());
		assertThat(updated.getScoringMode()).isEqualTo(input.getScoringMode());
		assertThat(updated.getRules()).isEqualTo(input.getRules());
		assertThat(updated.isActive()).isTrue();
		verify(_sportStorage).save(existing);
	}

	@Test
	void shouldAllowSportToKeepItsName()
	{
		final Sport existing = TestData.SPORTS.tennis();
		when(_sportStorage.findById(TestData.SPORTS.TENNIS_ID)).thenReturn(Optional.of(existing));
		when(_sportStorage.findByName("Tennis")).thenReturn(Optional.of(existing));
		when(_sportStorage.save(existing)).thenReturn(existing);

		_sportService.update(TestData.SPORTS.TENNIS_ID, TestData.SPORTS.newTennis());

		verify(_sportStorage).save(existing);
	}

	@Test
	void shouldThrowDuplicateWhenUpdatingToNameOfAnotherSport()
	{
		final Sport input = TestData.SPORTS.newTennis();
		input.setName("Chess");
		when(_sportStorage.findById(TestData.SPORTS.TENNIS_ID)).thenReturn(Optional.of(TestData.SPORTS.tennis()));
		when(_sportStorage.findByName("Chess")).thenReturn(Optional.of(TestData.SPORTS.chess()));

		assertThatThrownBy(() -> _sportService.update(TestData.SPORTS.TENNIS_ID, input))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Sport with name Chess already exists");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldThrowNotFoundWhenUpdatingMissingSport()
	{
		when(_sportStorage.findById("999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> _sportService.update("999", TestData.SPORTS.newTennis()))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldSoftDeleteSport()
	{
		final Sport tennis = TestData.SPORTS.tennis();
		when(_sportStorage.findById(TestData.SPORTS.TENNIS_ID)).thenReturn(Optional.of(tennis));

		_sportService.delete(TestData.SPORTS.TENNIS_ID);

		assertThat(tennis.isActive()).isFalse();
		verify(_sportStorage).save(tennis);
		verify(_sportStorage, never()).deleteById(anyString());
	}

	@Test
	void shouldThrowNotFoundWhenDeletingMissingSport()
	{
		when(_sportStorage.findById("999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> _sportService.delete("999"))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldRestoreInactiveSport()
	{
		final Sport chess = TestData.SPORTS.chess();
		chess.setActive(false);
		when(_sportStorage.findById(TestData.SPORTS.CHESS_ID)).thenReturn(Optional.of(chess));
		when(_sportStorage.save(chess)).thenReturn(chess);

		final Sport restored = _sportService.restore(TestData.SPORTS.CHESS_ID);

		assertThat(restored.isActive()).isTrue();
		verify(_sportStorage).save(chess);
	}

	@Test
	void shouldThrowInvalidOperationWhenRestoringActiveSport()
	{
		when(_sportStorage.findById(TestData.SPORTS.TENNIS_ID)).thenReturn(Optional.of(TestData.SPORTS.tennis()));

		assertThatThrownBy(() -> _sportService.restore(TestData.SPORTS.TENNIS_ID))
				.isInstanceOf(InvalidOperationException.class)
				.hasMessage("Sport with id 1 is already active");

		verify(_sportStorage, never()).save(any());
	}
}
