import {get} from 'svelte/store';
import {authStore} from './auth.store';

export function requireAuth(): boolean {
    if (!get(authStore).accessToken) {
        window.location.href = '#/login';
        return false;
    }
    return true;
}
