package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * Kratak prikaz sporta kad se pominje unutar drugog odgovora (npr. u meču).
 */
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SportSummaryDto
{
	@Schema(example = "1")
	String id;
	@Schema(example = "Tenis")
	String name;
}
