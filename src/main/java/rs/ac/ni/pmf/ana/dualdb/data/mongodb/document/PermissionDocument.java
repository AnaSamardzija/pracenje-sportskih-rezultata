package rs.ac.ni.pmf.ana.dualdb.data.mongodb.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDocument
{
	@Id
	private String id;

	@Indexed(name = "uk_permissions_name", unique = true, collation = "{ 'locale': 'en', 'strength': 1 }")
	private String name;

	private String description;
}
