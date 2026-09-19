package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Group implements DataModel
{
	String id;

	String name;
	String description;

	String createdBy;
	LocalDateTime createdAt;
}
