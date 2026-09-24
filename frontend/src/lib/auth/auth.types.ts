export type StorageType = 'MARIADB' | 'MONGODB';

// storageType se čuva pored tokena da bi UI znao na koju bazu je korisnik prijavljen (token se ne dekodira)
export type AuthState = {
    accessToken: string | null;
    storageType: StorageType | null;
}
