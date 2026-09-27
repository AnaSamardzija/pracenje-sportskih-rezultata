package rs.ac.ni.pmf.marko.dualdb.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.Set;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserDto
{
	@Schema(example = "2")
	String id;
	@Schema(example = "pera")
	String username;
	@Schema(example = "Pera")
	String firstName;
	@Schema(example = "Peric")
	String lastName;
	@Schema(example = "pera@example.com")
	String email;
	@Builder.Default
	@Schema(example = "[\"USER\"]")
	Set<String> roles = Collections.emptySet();
	@Schema(example = "true")
	boolean active;
}
