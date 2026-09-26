package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Izvedeni prikaz člana grupe (članstvo + username korisnika).
 * Nije entitet — koristi se za listu članova i za odgovor na dodavanje člana.
 */
@Data
@Builder
public class MemberView
{
	String userId;
	String username;
	GroupRole roleInGroup;
	LocalDateTime joinedAt;
}
