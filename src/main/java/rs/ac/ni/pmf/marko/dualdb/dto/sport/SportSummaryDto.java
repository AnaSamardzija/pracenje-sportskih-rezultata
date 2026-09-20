package rs.ac.ni.pmf.marko.dualdb.dto.sport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * Kratak prikaz sporta kad se pominje unutar drugog odgovora (npr. u meču).
 */
@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class SportSummaryDto
{
	String id;
	String name;
}
