import {get} from 'svelte/store';
import {push} from 'svelte-spa-router';
import {authStore} from '../auth/auth.store';
import {logout} from '../auth/auth.service';

// Nemamo refresh token: na 401 (istekao/pokvaren token) korisnik se odjavljuje i vraća na login
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

    if (res.status === 401) {
        logout();
        push('/login');
    }

    return res;
}
