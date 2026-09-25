package rs.ac.ni.pmf.marko.dualdb.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class GroupRequest
{
	@NotBlank(message = "name is required")
	@Schema(example = "Tenis kvarta")
	String name;

	@Schema(example = "subotnji tenis")
	String description;
}
