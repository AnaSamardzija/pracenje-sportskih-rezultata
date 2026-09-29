import type {AuthState} from '../types/auth.types';

const AUTH_KEY = 'auth';

export function saveAuth(data: AuthState) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(data));
}

// Sesija sačuvana pre nego što su korisnik i njegove permisije dodati u stanje tretira se kao odjava,
// da posle prijave svi prikazi rade sa permisijama
export function loadAuth(): AuthState | null {
    const data = localStorage.getItem(AUTH_KEY);
    const state: AuthState | null = data ? JSON.parse(data) : null;
    return state?.user?.permissions ? state : null;
}

export function clearAuth() {
    localStorage.removeItem(AUTH_KEY);
}
