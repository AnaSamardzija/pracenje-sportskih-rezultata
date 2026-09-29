package rs.ac.ni.pmf.ana.dualdb.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbRoleRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.PermissionDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.RoleDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.SportDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.SportRulesDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.UserDocument;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoPermissionRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoRoleRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoSportRepository;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.repository.MongoUserRepository;
import rs.ac.ni.pmf.ana.dualdb.model.Permission;
import rs.ac.ni.pmf.ana.dualdb.model.ScoringMode;
import rs.ac.ni.pmf.ana.dualdb.model.SportType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner
{
	private final MariaDbUserRepository _mariaDbUserRepository;
	private final MongoUserRepository _mongoUserRepository;

	private final MariaDbRoleRepository _mariaDbRoleRepository;

	private final MongoRoleRepository _mongoRoleRepository;
	private final MongoPermissionRepository _mongoPermissionRepository;
	private final MongoSportRepository _mongoSportRepository;
	private final MongoTemplate _mongoTemplate;

	private final PasswordEncoder _passwordEncoder;

	private final Map<String, Set<Permission>> rolePermissions = Map.of(
			"SYSTEM_ADMIN", EnumSet.allOf(Permission.class),
			"USER", EnumSet.of(Permission.USERS_PASSWORD_CHANGE_SELF)
	);

	@Override
	@Transactional
	public void run(final String @NonNull ... args)
	{
		initializeJpa();
		initializeMongoDb();
	}

	public void initializeJpa()
	{
		log.info("Checking if the admin user exists.");
		if (!_mariaDbUserRepository.existsByUsername("admin"))
		{
			log.info("Creating admin user.");
			_mariaDbUserRepository.save(createJpaAdmin());
		}
	}

	private UserEntity createJpaAdmin()
	{
		return UserEntity.builder()
				.username("admin")
				.password(_passwordEncoder.encode("admin.123"))
				.firstName("Ana")
				.lastName("Samardžija")
				.email("ana.samardzija@pmf.edu.rs")
				.roles(_mariaDbRoleRepository.findAllByNameIn(Set.of("SYSTEM_ADMIN", "USER")))
				.active(true)
				.build();
	}

	public void initializeMongoDb()
	{
		log.info("Checking and creating permissions in MongoDB, if needed.");
		createMongoPermissions();

		log.info("Checking and creating roles in MongoDB, if needed.");
		createMongoRoles();

		log.info("Checking and migrating MongoDB users to role references, if needed.");
		migrateMongoUserRoles();

		log.info("Checking and activating MongoDB users without the active field, if needed.");
		migrateMongoUserActive();

		log.info("Checking if the admin user exists in MongoDB.");
		if (_mongoUserRepository.findByUsername("admin").isEmpty())
		{
			log.info("Creating admin user in MongoDB");
			final UserDocument userDocument = UserDocument.builder()
					.username("admin")
					.password(_passwordEncoder.encode("admin.123"))
					.firstName("Ana")
					.lastName("Samardžija")
					.email("ana.samardzija@pmf.edu.rs")
					.roles(_mongoRoleRepository.findAllByNameIn(rolePermissions.keySet()))
					.createdAt(LocalDateTime.now())
					.active(true)
					.build();
			_mongoUserRepository.save(userDocument);
		}

		log.info("Checking and creating the sport catalog in MongoDB, if needed.");
		createMongoSports();
	}

	/**
	 * Pandan migracije V15: permisije i uloge se upisuju samo u praznu kolekciju, kao i sportovi.
	 */
	private void createMongoPermissions()
	{
		if (_mongoPermissionRepository.count() > 0)
		{
			return;
		}

		_mongoPermissionRepository.saveAll(Arrays.stream(Permission.values())
				.map(permission -> PermissionDocument.builder()
						.name(permission.getValue())
						.description(permission.getDescription())
						.build())
				.toList());

		log.info("Permissions created in MongoDB");
	}

	private void createMongoRoles()
	{
		if (_mongoRoleRepository.count() > 0)
		{
			return;
		}

		rolePermissions.forEach((role, permissions) -> _mongoRoleRepository.save(RoleDocument.builder()
				.name(role)
				.permissions(permissions.stream()
						.map(permission -> _mongoPermissionRepository.findByName(permission.getValue()))
						.flatMap(Optional::stream)
						.collect(Collectors.toSet()))
				.build()));

		log.info("Roles created in MongoDB");
	}

	/**
	 * Pandan migracije V14: sportovi se upisuju samo u praznu kolekciju, jer se Flyway migracija izvrši
	 * samo jednom. Provera po imenu bi posle restarta ponovo napravila sport koji je admin preimenovao.
	 */
	private void createMongoSports()
	{
		if (_mongoSportRepository.count() > 0)
		{
			return;
		}

		_mongoSportRepository.saveAll(List.of(
				sport("Tennis", SportType.INDIVIDUAL, ScoringMode.SETS, false, 1, 1, 3, 6, 3, 1, 0),
				sport("Table Tennis", SportType.INDIVIDUAL, ScoringMode.SETS, false, 1, 1, 5, 11, 3, 1, 0),
				sport("Chess", SportType.INDIVIDUAL, ScoringMode.OUTCOME, true, 1, 1, null, null, 2, 1, 0),
				sport("Football", SportType.TEAM, ScoringMode.POINTS, true, 1, 11, null, null, 3, 1, 0),
				sport("Basketball", SportType.TEAM, ScoringMode.POINTS, false, 1, 5, null, null, 3, 1, 0),
				sport("Volleyball", SportType.TEAM, ScoringMode.SETS, false, 2, 6, 5, 25, 3, 1, 0)
		));

		log.info("Sport catalog created in MongoDB");
	}

	private SportDocument sport(final String name, final SportType type, final ScoringMode scoringMode,
			final boolean allowDraw, final int minPlayersPerSide, final Integer maxPlayersPerSide,
			final Integer bestOf, final Integer pointsToWinSet,
			final int pointsForWin, final int pointsForDraw, final int pointsForLoss)
	{
		final SportRulesDocument rules = SportRulesDocument.builder()
				.allowDraw(allowDraw)
				.minPlayersPerSide(minPlayersPerSide)
				.maxPlayersPerSide(maxPlayersPerSide)
				.bestOf(bestOf)
				.pointsToWinSet(pointsToWinSet)
				.pointsForWin(pointsForWin)
				.pointsForDraw(pointsForDraw)
				.pointsForLoss(pointsForLoss)
				.build();

		return SportDocument.builder()
				.name(name)
				.type(type)
				.scoringMode(scoringMode)
				.rules(rules)
				.active(true)
				.build();
	}

	/**
	 * Uloge u korisničkom dokumentu su ranije bile niz imena (npr. ["USER"]) uz prepisan niz
	 * permisija. Sada su reference na kolekciju roles.
	 */
	private void migrateMongoUserRoles()
	{
		final Query oldFormat = Query.query(Criteria.where("roles").type(JsonSchemaObject.Type.STRING));

		_mongoTemplate.find(oldFormat, Document.class, "users").forEach(user -> {
			final List<String> roleNames = user.getList("roles", String.class);
			final List<ObjectId> roleIds = roleNames.stream()
					.map(_mongoRoleRepository::findByName)
					.filter(Optional::isPresent)
					.map(role -> new ObjectId(role.get().getId()))
					.toList();

			log.info("Migrating roles of MongoDB user {}: {}", user.getString("username"), roleNames);

			final Update update = new Update().set("roles", roleIds).unset("permissions");
			_mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(user.get("_id"))), update, "users");
		});
	}

	/**
	 * Pandan migracije V13: korisnici upisani pre uvođenja polja active postaju aktivni, kao
	 * DEFAULT TRUE u MariaDB-u. Bez toga bi se pročitali kao neaktivni i ne bi mogli da se prijave.
	 */
	private void migrateMongoUserActive()
	{
		final Query withoutActive = Query.query(Criteria.where("active").exists(false));
		_mongoTemplate.updateMulti(withoutActive, new Update().set("active", true), "users");
	}
}
