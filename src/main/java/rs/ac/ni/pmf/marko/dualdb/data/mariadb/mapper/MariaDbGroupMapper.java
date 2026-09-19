package rs.ac.ni.pmf.marko.dualdb.data.mariadb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.GroupEntity;
import rs.ac.ni.pmf.marko.dualdb.data.mariadb.entity.UserEntity;
import rs.ac.ni.pmf.marko.dualdb.model.Group;

@Component
public class MariaDbGroupMapper
{
	public Group toModel(final GroupEntity entity)
	{
		return Group.builder()
				.id(String.valueOf(entity.getId()))
				.name(entity.getName())
				.description(entity.getDescription())
				.createdBy(entity.getCreatedBy() == null ? null : String.valueOf(entity.getCreatedBy().getId()))
				.createdAt(entity.getCreatedAt())
				.build();
	}

	public GroupEntity toEntity(final Group group, final UserEntity createdBy)
	{
		return GroupEntity.builder()
				.id(group.getId() == null ? null : Long.parseLong(group.getId()))
				.name(group.getName())
				.description(group.getDescription())
				.createdBy(createdBy)
				.build();
	}
}
