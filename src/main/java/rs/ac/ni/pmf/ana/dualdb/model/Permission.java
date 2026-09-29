package rs.ac.ni.pmf.ana.dualdb.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission
{
	USERS_READ_ALL("users.read_all", "Read all users"),
	USERS_MODIFY("users.modify", "Modify another user's name and email"),
	USERS_ROLES_ASSIGN("users.roles.assign", "Change user roles"),
	USERS_DEACTIVATE("users.deactivate", "Deactivate users"),
	USERS_RESTORE("users.restore", "Restore deactivated users"),
	USERS_PASSWORD_CHANGE_SELF("users.password.change_self", "Change own password"),

	SPORTS_READ_INACTIVE("sports.read_inactive", "Read deleted (inactive) sports"),
	SPORTS_CREATE("sports.create", "Create sports"),
	SPORTS_UPDATE("sports.update", "Modify sports"),
	SPORTS_DELETE("sports.delete", "Delete (deactivate) sports"),
	SPORTS_RESTORE("sports.restore", "Restore deleted sports"),

	GROUPS_UPDATE_ANY("groups.update_any", "Modify any group without being its admin"),
	GROUPS_DELETE_ANY("groups.delete_any", "Delete any group without being its admin"),
	GROUPS_MEMBERS_ADD_ANY("groups.members.add_any", "Add members to any group without being its admin"),
	GROUPS_MEMBERS_KICK_ANY("groups.members.kick_any", "Remove members from any group without being its admin"),

	MATCHES_CREATE_ANY("matches.create_any", "Record matches in any group without being its member"),
	MATCHES_UPDATE_ANY("matches.update_any", "Modify matches in any group without being its admin"),
	MATCHES_DELETE_ANY("matches.delete_any", "Delete matches in any group without being its admin");

	private final String value;
	private final String description;
}
