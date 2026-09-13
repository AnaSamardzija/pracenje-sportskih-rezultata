package rs.ac.ni.pmf.marko.dualdb.dto.common;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class ErrorDto
{
	OffsetDateTime timestamp;
	String message;
}
