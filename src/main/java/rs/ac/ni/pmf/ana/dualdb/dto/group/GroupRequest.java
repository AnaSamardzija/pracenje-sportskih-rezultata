package rs.ac.ni.pmf.ana.dualdb.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
	@Size(max = 100, message = "name must be at most 100 characters")
	@Schema(example = "Saturday Tennis")
	String name;

	@Size(max = 255, message = "description must be at most 255 characters")
	@Schema(example = "Tennis with neighbors every Saturday")
	String description;
}
