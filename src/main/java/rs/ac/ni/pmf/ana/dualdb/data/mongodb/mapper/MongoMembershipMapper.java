package rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.MembershipDocument;
import rs.ac.ni.pmf.ana.dualdb.model.Membership;

import java.time.LocalDateTime;

@Component
public class MongoMembershipMapper
{
	public Membership toModel(final MembershipDocument document)
	{
		return Membership.builder()
				.id(document.getId())
				.userId(document.getUserId())
				.groupId(document.getGroupId())
				.roleInGroup(document.getRoleInGroup())
				.joinedAt(document.getJoinedAt())
				.build();
	}

	/**
	 * Vreme pridruživanja se postavlja samo za novo članstvo.
	 */
	public MembershipDocument toDocument(final Membership membership)
	{
		return MembershipDocument.builder()
				.id(membership.getId())
				.userId(membership.getUserId())
				.groupId(membership.getGroupId())
				.roleInGroup(membership.getRoleInGroup())
				.joinedAt(membership.getJoinedAt() == null ? LocalDateTime.now() : membership.getJoinedAt())
				.build();
	}
}
