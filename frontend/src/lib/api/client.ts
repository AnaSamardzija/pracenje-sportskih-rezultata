import {get} from 'svelte/store';
import {push} from 'svelte-spa-router';
import {authStore} from '../auth/auth.store';
import {logout} from '../auth/auth.service';

// Nemamo refresh token: na 401 (istekao/pokvaren token) korisnik se odjavljuje i vraća na login.
// Odjava samo kad je token poslat, jer i login sa pogrešnom lozinkom vraća 401.
export async function apiFetch(url: string, options: RequestInit = {}) {
    const {accessToken} = get(authStore);

    const res = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(accessToken && {Authorization: `Bearer ${accessToken}`}),
            ...(options.headers ?? {})
        }
    });

    if (res.status === 401 && accessToken) {
        logout();
        push('/login');
    }

    return res;
}

// Backend na grešku vraća ErrorDto {timestamp, message, path}; ako poruke nema, koristi se podrazumevana
export async function errorMessage(res: Response, fallback: string): Promise<string> {
    const body = await res.json().catch(() => null);
    return body?.message ?? fallback;
}
