package rs.ac.ni.pmf.marko.dualdb.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PlayerDto
{
	@Schema(example = "2")
	String id;
	@Schema(example = "pera")
	String username;
}
