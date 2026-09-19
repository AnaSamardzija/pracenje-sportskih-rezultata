package rs.ac.ni.pmf.marko.dualdb.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class GroupResponse
{
	String id;
	String name;
	String description;
	String createdBy;
	LocalDateTime createdAt;
}
