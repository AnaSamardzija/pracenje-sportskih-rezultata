import type {AuthState} from './auth.types';

const AUTH_KEY = 'auth';

export function saveAuth(data: AuthState) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(data));
}

export function loadAuth(): AuthState | null {
    const data = localStorage.getItem(AUTH_KEY);
    return data ? JSON.parse(data) : null;
}

export function clearAuth() {
    localStorage.removeItem(AUTH_KEY);
}
