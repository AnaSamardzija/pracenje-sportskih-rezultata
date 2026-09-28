export interface UserDto {
    id: string;
    username: string;
    firstName: string | null;
    lastName: string | null;
    email: string;
    roles: string[];
    active: boolean;
}

// Izostavljeno (null) ime ili prezime backend briše, pa se prazno polje šalje kao null
export interface UpdateProfileRequest {
    firstName: string | null;
    lastName: string | null;
    email: string;
}

// Izmena tuđeg naloga (samo SYSTEM_ADMIN); username se ne menja, a uloga mora biti bar jedna
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

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
}

// Uloga iz /users/me; služi samo za prikaz, backend svaku radnju proverava sam
export function isSystemAdmin(user: UserDto | null | undefined): boolean {
    return user?.roles.includes('SYSTEM_ADMIN') ?? false;
}

// Ime i prezime za prikaz; oba nisu obavezna, pa može biti i prazan string
export function fullNameOf(user: UserDto): string {
    return [user.firstName, user.lastName].filter(Boolean).join(' ');
}

// Inicijali za avatar; ime i prezime nisu obavezni, pa je rezerva prvo slovo username-a
export function userInitials(user: UserDto): string {
    return ((user.firstName?.[0] ?? '') + (user.lastName?.[0] ?? '') || user.username[0]).toUpperCase();
}
