export interface UserDto {
    id: string;
    username: string;
    firstName: string | null;
    lastName: string | null;
    email: string;
    roles: string[];
    permissions: string[];
    active: boolean;
}

// Izostavljeno (null) ime ili prezime backend briše, pa se prazno polje šalje kao null
export interface UpdateProfileRequest {
    firstName: string | null;
    lastName: string | null;
    email: string;
}

// Izmena tuđeg naloga (permisija users.modify, a promena uloga i users.roles.assign); username se ne menja,
// a uloga mora biti bar jedna
export interface UpdateUserRequest {
    firstName: string | null;
    lastName: string | null;
    email: string;
    roles: Role[];
}

export const ROLES = ['USER', 'SYSTEM_ADMIN'] as const;
export type Role = typeof ROLES[number];

export const ROLE_LABELS: Record<Role, string> = {
    USER: 'User',
    SYSTEM_ADMIN: 'System admin'
};

export const ROLE_HINTS: Record<Role, string> = {
    USER: 'Plays, joins groups and records matches.',
    SYSTEM_ADMIN: 'Full access: manages users and sports, and every group and match.'
};

// Vrednosti su tačno enum Permission sa backend-a (i migracija V15)
export type Permission =
    | 'users.read_all'
    | 'users.modify'
    | 'users.roles.assign'
    | 'users.deactivate'
    | 'users.restore'
    | 'users.password.change_self'
    | 'sports.read_inactive'
    | 'sports.create'
    | 'sports.update'
    | 'sports.delete'
    | 'sports.restore'
    | 'groups.update_any'
    | 'groups.delete_any'
    | 'groups.members.add_any'
    | 'groups.members.kick_any'
    | 'matches.create_any'
    | 'matches.update_any'
    | 'matches.delete_any';

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
}

// Permisije iz /users/me; služe samo za prikaz, backend svaku radnju proverava sam
export function hasPermission(user: UserDto | null | undefined, permission: Permission): boolean {
    return user?.permissions.includes(permission) ?? false;
}

// Ime i prezime za prikaz; oba nisu obavezna, pa može biti i prazan string
export function fullNameOf(user: UserDto): string {
    return [user.firstName, user.lastName].filter(Boolean).join(' ');
}

// Inicijali za avatar; ime i prezime nisu obavezni, pa je rezerva prvo slovo username-a
export function userInitials(user: UserDto): string {
    return ((user.firstName?.[0] ?? '') + (user.lastName?.[0] ?? '') || user.username[0]).toUpperCase();
}
