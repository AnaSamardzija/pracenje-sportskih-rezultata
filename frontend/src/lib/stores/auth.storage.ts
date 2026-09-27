import type {AuthState} from '../types/auth.types';

const AUTH_KEY = 'auth';

export function saveAuth(data: AuthState) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(data));
}

// Sesija sačuvana pre nego što je korisnik dodat u stanje nema user, pa se tretira kao odjava
export function loadAuth(): AuthState | null {
    const data = localStorage.getItem(AUTH_KEY);
    const state: AuthState | null = data ? JSON.parse(data) : null;
    return state?.user ? state : null;
}

export function clearAuth() {
    localStorage.removeItem(AUTH_KEY);
}
