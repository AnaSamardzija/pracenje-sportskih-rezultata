package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Membership implements DataModel
{
	String id;

	String userId;
	String groupId;

	GroupRole roleInGroup;
	LocalDateTime joinedAt;
}
