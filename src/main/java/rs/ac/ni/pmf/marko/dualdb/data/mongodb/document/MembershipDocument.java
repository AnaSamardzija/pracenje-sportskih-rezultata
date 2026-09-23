package rs.ac.ni.pmf.marko.dualdb.data.mongodb.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import rs.ac.ni.pmf.marko.dualdb.model.GroupRole;

import java.time.LocalDateTime;

/**
 * Korisnik može biti član grupe samo jednom — isto kao uk_memberships_user_group u MariaDB.
 */
@Document(collection = "memberships")
@CompoundIndex(name = "uk_memberships_user_group", def = "{ 'userId': 1, 'groupId': 1 }", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipDocument
{
	@Id
	private String id;

	private String userId;

	@Indexed(name = "ix_memberships_group")
	private String groupId;

	private GroupRole roleInGroup;
	private LocalDateTime joinedAt;
}
