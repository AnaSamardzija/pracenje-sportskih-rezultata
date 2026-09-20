UPDATE roles SET name = 'SYSTEM_ADMIN' WHERE name = 'ADMIN';

DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE name = 'MANAGER');

DELETE FROM roles_permissions WHERE role_id IN (SELECT id FROM roles WHERE name = 'MANAGER');

DELETE FROM roles WHERE name = 'MANAGER';

DELETE FROM permissions WHERE name IN ('content.update', 'content.delete');
