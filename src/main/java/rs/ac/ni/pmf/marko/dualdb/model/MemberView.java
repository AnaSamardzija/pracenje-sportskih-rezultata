package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Izvedeni prikaz člana grupe (članstvo + username i aktivnost korisnika).
 * Nije entitet — koristi se za listu članova i za odgovor na dodavanje člana.
 */
@Data
@Builder
public class MemberView
{
	String userId;
	String username;
	boolean active;
	GroupRole roleInGroup;
	LocalDateTime joinedAt;
}
