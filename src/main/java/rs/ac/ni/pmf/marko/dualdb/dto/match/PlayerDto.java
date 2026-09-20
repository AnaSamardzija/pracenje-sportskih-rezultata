package rs.ac.ni.pmf.marko.dualdb.dto.match;

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
	String id;
	String username;
}
