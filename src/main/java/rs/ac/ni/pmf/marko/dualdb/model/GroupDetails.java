package rs.ac.ni.pmf.marko.dualdb.model;

import lombok.Builder;
import lombok.Data;

/**
 * Izvedeni prikaz grupe: grupa + broj članova + uloga trenutnog korisnika u njoj.
 * Nije entitet — sklapa se u servisu iz grupe i članstava.
 */
@Data
@Builder
public class GroupDetails
{
	Group group;
	long memberCount;
	GroupRole myRole;
}
