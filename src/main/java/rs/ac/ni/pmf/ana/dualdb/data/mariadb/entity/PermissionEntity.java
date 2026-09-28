package rs.ac.ni.pmf.ana.dualdb.data.mariadb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column
	private String description;

	@ManyToMany(mappedBy = "permissions")
	@Builder.Default
	private Set<RoleEntity> roles = new HashSet<>();
}
