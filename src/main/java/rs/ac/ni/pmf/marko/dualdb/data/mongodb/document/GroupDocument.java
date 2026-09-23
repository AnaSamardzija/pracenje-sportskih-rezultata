package rs.ac.ni.pmf.marko.dualdb.data.mongodb.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDocument
{
	@Id
	private String id;

	private String name;
	private String description;

	private String createdBy;
	private LocalDateTime createdAt;
}
