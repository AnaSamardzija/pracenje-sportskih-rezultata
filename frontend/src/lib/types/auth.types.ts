import type {UserDto} from './user.types';

// Vrednosti su tačno enum StorageType sa backend-a
export type StorageType = 'MARIADB' | 'MONGODB';

export interface AuthRequest {
    username: string;
    password: string;
    storageType: StorageType;
}

// Ime i prezime nisu obavezni; prazno polje se šalje kao null
export interface RegisterRequest {
    username: string;
    password: string;
    firstName: string | null;
    lastName: string | null;
    email: string;
    storageType: StorageType;
}

export interface AuthResponse {
    accessToken: string;
}

// storageType se čuva pored tokena da bi UI znao na koju bazu je korisnik prijavljen (token se ne dekodira),
// a user (odgovor /users/me posle prijave) da bi znao uloge, npr. da li da prikaže admin dugmad
export type AuthState = {
    accessToken: string | null;
    storageType: StorageType | null;
    user: UserDto | null;
}

export const STORAGE_LABELS: Record<StorageType, string> = {
    MARIADB: 'MariaDB',
    MONGODB: 'MongoDB'
};
