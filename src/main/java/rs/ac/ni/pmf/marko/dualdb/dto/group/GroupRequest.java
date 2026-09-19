package rs.ac.ni.pmf.marko.dualdb.dto.group;

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
	String name;

	String description;
}
