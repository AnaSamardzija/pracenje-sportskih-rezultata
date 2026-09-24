// Vrednosti su tačno enum StorageType sa backend-a
export type StorageType = 'MARIADB' | 'MONGODB';

export interface AuthRequest {
    username: string;
    password: string;
    storageType: StorageType;
}

export interface AuthResponse {
    accessToken: string;
}

// storageType se čuva pored tokena da bi UI znao na koju bazu je korisnik prijavljen (token se ne dekodira)
export type AuthState = {
    accessToken: string | null;
    storageType: StorageType | null;
}

export const STORAGE_LABELS: Record<StorageType, string> = {
    MARIADB: 'MariaDB',
    MONGODB: 'MongoDB'
};
