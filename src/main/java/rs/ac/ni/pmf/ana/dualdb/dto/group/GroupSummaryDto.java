package rs.ac.ni.pmf.ana.dualdb.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * Kratak prikaz grupe kad se pominje unutar drugog odgovora (npr. u meču).
 */
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GroupSummaryDto
{
	@Schema(example = "1")
	String id;
	@Schema(example = "Tenis kvarta")
	String name;
}
