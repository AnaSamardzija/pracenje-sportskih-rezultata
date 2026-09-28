package rs.ac.ni.pmf.ana.dualdb.data.mongodb.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.data.mongodb.document.GroupDocument;
import rs.ac.ni.pmf.ana.dualdb.model.Group;

import java.time.LocalDateTime;

@Component
public class MongoGroupMapper
{
	public Group toModel(final GroupDocument document)
	{
		return Group.builder()
				.id(document.getId())
				.name(document.getName())
				.description(document.getDescription())
				.createdBy(document.getCreatedBy())
				.createdAt(document.getCreatedAt())
				.build();
	}

	/**
	 * Mongo nema @CreationTimestamp, pa vreme kreiranja postavlja mapper samo za novu grupu.
	 */
	public GroupDocument toDocument(final Group group)
	{
		return GroupDocument.builder()
				.id(group.getId())
				.name(group.getName())
				.description(group.getDescription())
				.createdBy(group.getCreatedBy())
				.createdAt(group.getCreatedAt() == null ? LocalDateTime.now() : group.getCreatedAt())
				.build();
	}
}
