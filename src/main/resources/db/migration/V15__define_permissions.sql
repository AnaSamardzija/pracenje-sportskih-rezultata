DELETE FROM roles_permissions;

DELETE FROM permissions;

INSERT INTO permissions (name, description)
VALUES ('users.read_all', 'Read all users'),
       ('users.modify', 'Modify another user''s name and email'),
       ('users.roles.assign', 'Change user roles'),
       ('users.deactivate', 'Deactivate users'),
       ('users.restore', 'Restore deactivated users'),
       ('users.password.change_self', 'Change own password'),
       ('sports.read_inactive', 'Read deleted (inactive) sports'),
       ('sports.create', 'Create sports'),
       ('sports.update', 'Modify sports'),
       ('sports.delete', 'Delete (deactivate) sports'),
       ('sports.restore', 'Restore deleted sports'),
       ('groups.update_any', 'Modify any group without being its admin'),
       ('groups.delete_any', 'Delete any group without being its admin'),
       ('groups.members.add_any', 'Add members to any group without being its admin'),
       ('groups.members.kick_any', 'Remove members from any group without being its admin'),
       ('matches.create_any', 'Record matches in any group without being its member'),
       ('matches.update_any', 'Modify matches in any group without being its admin'),
       ('matches.delete_any', 'Delete matches in any group without being its admin');

INSERT IGNORE INTO roles (name)
VALUES ('SYSTEM_ADMIN'),
       ('USER');

INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN';

INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.name = 'users.password.change_self';
