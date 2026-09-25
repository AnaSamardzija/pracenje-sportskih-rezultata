package rs.ac.ni.pmf.marko.dualdb.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class ErrorDto
{
	@Schema(example = "2026-09-20T12:00:00.000+02:00")
	OffsetDateTime timestamp;
	@Schema(example = "Sport with id 999999 not found")
	String message;
}
