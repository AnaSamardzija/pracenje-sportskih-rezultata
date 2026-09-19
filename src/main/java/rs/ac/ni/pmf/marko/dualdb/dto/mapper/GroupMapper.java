package rs.ac.ni.pmf.marko.dualdb.dto.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.dto.group.GroupRequest;
import rs.ac.ni.pmf.marko.dualdb.dto.group.GroupResponse;
import rs.ac.ni.pmf.marko.dualdb.dto.group.MemberResponse;
import rs.ac.ni.pmf.marko.dualdb.model.Group;
import rs.ac.ni.pmf.marko.dualdb.model.MemberView;

@Component
public class GroupMapper
{
	public GroupResponse toResponse(final Group group)
	{
		return GroupResponse.builder()
				.id(group.getId())
				.name(group.getName())
				.description(group.getDescription())
				.createdBy(group.getCreatedBy())
				.createdAt(group.getCreatedAt())
				.build();
	}

	public Group toModel(final GroupRequest request)
	{
		return Group.builder()
				.name(request.getName())
				.description(request.getDescription())
				.build();
	}

	public MemberResponse toMemberResponse(final MemberView member)
	{
		return MemberResponse.builder()
				.userId(member.getUserId())
				.username(member.getUsername())
				.roleInGroup(member.getRoleInGroup())
				.joinedAt(member.getJoinedAt())
				.build();
	}
}
