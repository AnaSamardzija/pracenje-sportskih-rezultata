package rs.ac.ni.pmf.marko.dualdb.data.mongodb.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDocument
{
	@Id
	private String id;

	@Indexed(name = "uk_roles_name", unique = true, collation = "{ 'locale': 'en', 'strength': 1 }")
	private String name;

	@DocumentReference
	@Builder.Default
	private Set<PermissionDocument> permissions = new HashSet<>();
}
