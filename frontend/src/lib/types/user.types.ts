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

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
}

// Uloga iz /users/me; služi samo za prikaz, backend svaku radnju proverava sam
export function isSystemAdmin(user: UserDto | null | undefined): boolean {
    return user?.roles.includes('SYSTEM_ADMIN') ?? false;
}

// Inicijali za avatar; ime i prezime nisu obavezni, pa je rezerva prvo slovo username-a
export function userInitials(user: UserDto): string {
    return ((user.firstName?.[0] ?? '') + (user.lastName?.[0] ?? '') || user.username[0]).toUpperCase();
}
