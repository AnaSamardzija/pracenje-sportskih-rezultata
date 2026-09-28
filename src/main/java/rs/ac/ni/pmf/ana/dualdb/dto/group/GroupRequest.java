package rs.ac.ni.pmf.ana.dualdb.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GroupRequest
{
	@NotBlank(message = "name is required")
	@Schema(example = "Saturday Tennis")
	String name;

	@Schema(example = "Tennis with neighbors every Saturday")
	String description;
}
