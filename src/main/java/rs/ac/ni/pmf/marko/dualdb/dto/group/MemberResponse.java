package rs.ac.ni.pmf.marko.dualdb.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import rs.ac.ni.pmf.marko.dualdb.model.GroupRole;

import java.time.LocalDateTime;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MemberResponse
{
	@Schema(example = "2")
	String userId;
	@Schema(example = "pera")
	String username;
	@Schema(example = "GROUP_ADMIN")
	GroupRole roleInGroup;
	@Schema(example = "2026-09-20T12:00:00")
	LocalDateTime joinedAt;
}
