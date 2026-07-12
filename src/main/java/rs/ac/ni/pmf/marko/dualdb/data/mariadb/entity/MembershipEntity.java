package rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import rs.ac.ni.pmf.marko.dualdb.model.GroupRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private GroupEntity group;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_in_group", nullable = false, length = 20)
	private GroupRole roleInGroup;

	@CreationTimestamp
	@Column(name = "joined_at", nullable = false, updatable = false)
	private LocalDateTime joinedAt;
}
