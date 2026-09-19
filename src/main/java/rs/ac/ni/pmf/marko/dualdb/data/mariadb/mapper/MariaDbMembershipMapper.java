package rs.ac.ni.pmf.marko.dualdb.data.mariadb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.GroupEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.MembershipEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.model.Membership;

@Component
public class MariaDbMembershipMapper
{
	public Membership toModel(final MembershipEntity entity)
	{
		return Membership.builder()
				.id(String.valueOf(entity.getId()))
				.userId(entity.getUser() == null ? null : String.valueOf(entity.getUser().getId()))
				.groupId(entity.getGroup() == null ? null : String.valueOf(entity.getGroup().getId()))
				.roleInGroup(entity.getRoleInGroup())
				.joinedAt(entity.getJoinedAt())
				.build();
	}

	public MembershipEntity toEntity(final Membership membership, final UserEntity user, final GroupEntity group)
	{
		return MembershipEntity.builder()
				.id(membership.getId() == null ? null : Long.parseLong(membership.getId()))
				.user(user)
				.group(group)
				.roleInGroup(membership.getRoleInGroup())
				.build();
	}
}
