package rs.ac.ni.pmf.marko.dualdb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.ni.pmf.marko.dualdb.TestData;
import rs.ac.ni.pmf.marko.dualdb.data.StorageType;
import rs.ac.ni.pmf.marko.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.marko.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.marko.dualdb.model.Sport;
import rs.ac.ni.pmf.marko.dualdb.storage.CurrentStorageTypeProvider;
import rs.ac.ni.pmf.marko.dualdb.storage.StorageResolver;
import rs.ac.ni.pmf.marko.dualdb.storage.sport.SportStorage;

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
		final Sport tenis = TestData.SPORTS.tenis();
		when(_sportStorage.findAllActive()).thenReturn(List.of(tenis));

		assertThat(_sportService.findAll(false)).containsExactly(tenis);
	}

	@Test
	void shouldReturnAllSportsWhenIncludeInactive()
	{
		final Sport tenis = TestData.SPORTS.tenis();
		final Sport sah = TestData.SPORTS.sah();
		sah.setActive(false);
		when(_sportStorage.findAll()).thenReturn(List.of(tenis, sah));

		assertThat(_sportService.findAll(true)).containsExactly(tenis, sah);
	}

	@Test
	void shouldReturnSportById()
	{
		final Sport tenis = TestData.SPORTS.tenis();
		when(_sportStorage.findById(TestData.SPORTS.TENIS_ID)).thenReturn(Optional.of(tenis));

		assertThat(_sportService.findById(TestData.SPORTS.TENIS_ID, false)).isEqualTo(tenis);
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
		final Sport sah = TestData.SPORTS.sah();
		sah.setActive(false);
		when(_sportStorage.findById(TestData.SPORTS.SAH_ID)).thenReturn(Optional.of(sah));

		assertThatThrownBy(() -> _sportService.findById(TestData.SPORTS.SAH_ID, false))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void shouldReturnInactiveSportWhenIncludeInactive()
	{
		final Sport sah = TestData.SPORTS.sah();
		sah.setActive(false);
		when(_sportStorage.findById(TestData.SPORTS.SAH_ID)).thenReturn(Optional.of(sah));

		assertThat(_sportService.findById(TestData.SPORTS.SAH_ID, true)).isEqualTo(sah);
	}

	@Test
	void shouldCreateSportAsActive()
	{
		final Sport input = TestData.SPORTS.newTenis();
		when(_sportStorage.findByName("Tenis")).thenReturn(Optional.empty());
		when(_sportStorage.save(input)).thenReturn(input);

		final Sport created = _sportService.create(input);

		assertThat(created.isActive()).isTrue();
		verify(_sportStorage).save(input);
	}

	@Test
	void shouldThrowDuplicateWhenNameIsTaken()
	{
		when(_sportStorage.findByName("Tenis")).thenReturn(Optional.of(TestData.SPORTS.tenis()));

		assertThatThrownBy(() -> _sportService.create(TestData.SPORTS.newTenis()))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Sport with name Tenis already exists");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldThrowDuplicateWithRestoreHintWhenNameBelongsToInactiveSport()
	{
		final Sport inactiveTenis = TestData.SPORTS.tenis();
		inactiveTenis.setActive(false);
		when(_sportStorage.findByName("Tenis")).thenReturn(Optional.of(inactiveTenis));

		assertThatThrownBy(() -> _sportService.create(TestData.SPORTS.newTenis()))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Sport with name Tenis already exists but is inactive; restore it instead");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldThrowInvalidOperationWhenMaxPlayersIsLessThanMin()
	{
		final Sport input = TestData.SPORTS.newTenis();
		input.getRules().setMinPlayersPerSide(2);
		when(_sportStorage.findByName("Tenis")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> _sportService.create(input))
				.isInstanceOf(InvalidOperationException.class)
				.hasMessage("maxPlayersPerSide cannot be less than minPlayersPerSide");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldUpdateSport()
	{
		final Sport existing = TestData.SPORTS.tenis();
		final Sport input = TestData.SPORTS.newFudbal();
		when(_sportStorage.findById(TestData.SPORTS.TENIS_ID)).thenReturn(Optional.of(existing));
		when(_sportStorage.findByName("Fudbal")).thenReturn(Optional.empty());
		when(_sportStorage.save(existing)).thenReturn(existing);

		final Sport updated = _sportService.update(TestData.SPORTS.TENIS_ID, input);

		assertThat(updated.getId()).isEqualTo(TestData.SPORTS.TENIS_ID);
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
		final Sport existing = TestData.SPORTS.tenis();
		when(_sportStorage.findById(TestData.SPORTS.TENIS_ID)).thenReturn(Optional.of(existing));
		when(_sportStorage.findByName("Tenis")).thenReturn(Optional.of(existing));
		when(_sportStorage.save(existing)).thenReturn(existing);

		_sportService.update(TestData.SPORTS.TENIS_ID, TestData.SPORTS.newTenis());

		verify(_sportStorage).save(existing);
	}

	@Test
	void shouldThrowDuplicateWhenUpdatingToNameOfAnotherSport()
	{
		final Sport input = TestData.SPORTS.newTenis();
		input.setName("Sah");
		when(_sportStorage.findById(TestData.SPORTS.TENIS_ID)).thenReturn(Optional.of(TestData.SPORTS.tenis()));
		when(_sportStorage.findByName("Sah")).thenReturn(Optional.of(TestData.SPORTS.sah()));

		assertThatThrownBy(() -> _sportService.update(TestData.SPORTS.TENIS_ID, input))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("Sport with name Sah already exists");

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldThrowNotFoundWhenUpdatingMissingSport()
	{
		when(_sportStorage.findById("999")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> _sportService.update("999", TestData.SPORTS.newTenis()))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(_sportStorage, never()).save(any());
	}

	@Test
	void shouldSoftDeleteSport()
	{
		final Sport tenis = TestData.SPORTS.tenis();
		when(_sportStorage.findById(TestData.SPORTS.TENIS_ID)).thenReturn(Optional.of(tenis));

		_sportService.delete(TestData.SPORTS.TENIS_ID);

		assertThat(tenis.isActive()).isFalse();
		verify(_sportStorage).save(tenis);
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
		final Sport sah = TestData.SPORTS.sah();
		sah.setActive(false);
		when(_sportStorage.findById(TestData.SPORTS.SAH_ID)).thenReturn(Optional.of(sah));
		when(_sportStorage.save(sah)).thenReturn(sah);

		final Sport restored = _sportService.restore(TestData.SPORTS.SAH_ID);

		assertThat(restored.isActive()).isTrue();
		verify(_sportStorage).save(sah);
	}

	@Test
	void shouldThrowInvalidOperationWhenRestoringActiveSport()
	{
		when(_sportStorage.findById(TestData.SPORTS.TENIS_ID)).thenReturn(Optional.of(TestData.SPORTS.tenis()));

		assertThatThrownBy(() -> _sportService.restore(TestData.SPORTS.TENIS_ID))
				.isInstanceOf(InvalidOperationException.class)
				.hasMessage("Sport with id 1 is already active");

		verify(_sportStorage, never()).save(any());
	}
}
