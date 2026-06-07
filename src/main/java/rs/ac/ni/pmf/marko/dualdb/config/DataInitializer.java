package rs.ac.ni.pmf.marko.dualdb.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.PermissionEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.RoleEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbPermissionRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbRoleRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.repository.MariaDbUserRepository;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.document.UserDocument;
import rs.ac.ni.pmf.marko.dualdb.data.mongodb.repository.MongoUserRepository;

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

	private final PasswordEncoder _passwordEncoder;

	private final Set<String> adminPermissions = Set.of(
			"user.create",
			"user.read",
			"user.update",
			"user.delete"
	);

	private final Set<String> managerPermissions = Set.of(
			"content.read",
			"content.update",
			"content.delete"
	);

	private final Set<String> userPermissions = Set.of(
			"content.read"
	);

	private final Map<String, Set<String>> rolePermissions = Map.of(
			"ADMIN", adminPermissions,
			"MANAGER", managerPermissions,
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
				.roles(_mariaDbRoleRepository.findAllByNameIn(Set.of("ADMIN", "MANAGER", "USER")))
				.build();
	}

	public void initializeMongoDb()
	{
		if (!_mongoUserRepository.existsByUsername("admin"))
		{
			log.info("Creating admin user in MongoDB");
			final UserDocument userDocument = UserDocument.builder()
					.username("admin")
					.password(_passwordEncoder.encode("admin.123"))
					.firstName("Marko")
					.lastName("Milošević")
					.email("marko.milosevic@pmf.edu.rs")
					.roles(rolePermissions.keySet())
					.permissions(rolePermissions.values().stream().flatMap(Set::stream).collect(Collectors.toSet()))
					.build();
			_mongoUserRepository.save(userDocument);
		}
	}
}
