package rs.ac.ni.pmf.marko.dualdb.config;

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
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.PermissionEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.RoleEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbPermissionRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbRoleRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.PermissionDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.RoleDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.UserDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository.MongoPermissionRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository.MongoRoleRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository.MongoUserRepository;

import java.time.LocalDateTime;
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
	private final MariaDbPermissionRepository _mariaDbPermissionRepository;

	private final MongoRoleRepository _mongoRoleRepository;
	private final MongoPermissionRepository _mongoPermissionRepository;
	private final MongoTemplate _mongoTemplate;

	private final PasswordEncoder _passwordEncoder;

	private final Set<String> adminPermissions = Set.of(
			"user.create",
			"user.read",
			"user.update",
			"user.delete"
	);

	private final Set<String> userPermissions = Set.of(
			"content.read"
	);

	private final Map<String, Set<String>> rolePermissions = Map.of(
			"SYSTEM_ADMIN", adminPermissions,
			"USER", userPermissions
	);

	@Override
	@Transactional
	public void run(final String @NonNull ... args)
	{
		// TODO: Initialize data for the application
		initializeJpa();
		initializeMongoDb();
	}

	public void initializeJpa()
	{
		log.info("Checking and creating permissions, if needed.");
		createPermissions();

		log.info("Checking and creating roles, if needed.");
		createRoles();

		log.info("Checking if the admin user exists.");
		if (!_mariaDbUserRepository.existsByUsername("admin"))
		{
			log.info("Creating admin user.");
			_mariaDbUserRepository.save(createJpaAdmin());
		}
	}

	private void createPermissions()
	{
		rolePermissions.values().stream()
				.flatMap(Set::stream)
				.forEach(this::createPermission);
	}

	private void createPermission(final String permission)
	{
		if (!_mariaDbPermissionRepository.existsByName(permission))
		{
			log.info("Creating permission: {}", permission);

			final PermissionEntity permissionEntity = PermissionEntity.builder().name(permission).build();
			_mariaDbPermissionRepository.save(permissionEntity);
		}
	}

	private void createRoles()
	{
		rolePermissions.forEach(this::createRole);
	}

	private void createRole(final String role, final Set<String> permissions)
	{
		if (!_mariaDbRoleRepository.existsByName(role))
		{
			log.info("Creating role: {}", role);
			final Set<PermissionEntity> rolePermissions = permissions.stream()
					.map(_mariaDbPermissionRepository::findByName)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toSet());
			final RoleEntity roleEntity = RoleEntity.builder()
					.name(role)
					.permissions(rolePermissions)
					.build();

			_mariaDbRoleRepository.save(roleEntity);
		}
	}

	private UserEntity createJpaAdmin()
	{
		return UserEntity.builder()
				.username("admin")
				.password(_passwordEncoder.encode("admin.123"))
				.firstName("Marko")
				.lastName("Milošević")
				.email("marko.milosevic@pmf.edu.rs")
				.roles(_mariaDbRoleRepository.findAllByNameIn(Set.of("SYSTEM_ADMIN", "USER")))
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

		final Optional<UserDocument> existingAdmin = _mongoUserRepository.findByUsername("admin");

		if (existingAdmin.isEmpty())
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
					.build();
			_mongoUserRepository.save(userDocument);

			return;
		}

		alignMongoAdminRoles(existingAdmin.get());
	}

	private void createMongoPermissions()
	{
		rolePermissions.values().stream()
				.flatMap(Set::stream)
				.forEach(this::createMongoPermission);
	}

	private void createMongoPermission(final String permission)
	{
		if (!_mongoPermissionRepository.existsByName(permission))
		{
			log.info("Creating permission in MongoDB: {}", permission);

			final PermissionDocument permissionDocument = PermissionDocument.builder().name(permission).build();
			_mongoPermissionRepository.save(permissionDocument);
		}
	}

	private void createMongoRoles()
	{
		rolePermissions.forEach(this::createMongoRole);
	}

	private void createMongoRole(final String role, final Set<String> permissions)
	{
		if (!_mongoRoleRepository.existsByName(role))
		{
			log.info("Creating role in MongoDB: {}", role);
			final Set<PermissionDocument> rolePermissions = permissions.stream()
					.map(_mongoPermissionRepository::findByName)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toSet());
			final RoleDocument roleDocument = RoleDocument.builder()
					.name(role)
					.permissions(rolePermissions)
					.build();

			_mongoRoleRepository.save(roleDocument);
		}
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
	 * MongoDB nema Flyway, pa ovde radimo ono što u MariaDB-u radi migracija: ako zatečeni
	 * admin nosi stare uloge (ADMIN/MANAGER), prepiši ih na uloge iz dizajna.
	 */
	private void alignMongoAdminRoles(final UserDocument admin)
	{
		final Set<String> adminRoles = admin.getRoles().stream()
				.map(RoleDocument::getName)
				.collect(Collectors.toSet());

		if (adminRoles.equals(rolePermissions.keySet()))
		{
			return;
		}

		log.info("Aligning admin roles in MongoDB: {} -> {}", adminRoles, rolePermissions.keySet());

		admin.setRoles(_mongoRoleRepository.findAllByNameIn(rolePermissions.keySet()));

		_mongoUserRepository.save(admin);
	}
}
