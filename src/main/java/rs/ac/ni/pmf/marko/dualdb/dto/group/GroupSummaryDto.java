package rs.ac.ni.pmf.marko.dualdb.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * Kratak prikaz grupe kad se pominje unutar drugog odgovora (npr. u meču).
 */
@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class GroupSummaryDto
{
	@Schema(example = "1")
	String id;
	@Schema(example = "Tenis kvarta")
	String name;
}
