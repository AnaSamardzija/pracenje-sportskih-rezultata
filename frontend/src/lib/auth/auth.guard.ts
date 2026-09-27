import {get} from 'svelte/store';
import {authStore} from './auth.store';

export function requireAuth(): boolean {
    if (!get(authStore).accessToken) {
        window.location.href = '#/login';
        return false;
    }
    return true;
}

// Stranice za goste (prijava, registracija): prijavljen korisnik se vraća na početnu,
// da ne bi video formu ispod navigacije ili novom registracijom tiho zamenio svoj nalog
export function requireGuest(): boolean {
    if (get(authStore).accessToken) {
        window.location.href = '#/';
        return false;
    }
    return true;
}

// Uloge su iz /users/me učitanog pri prijavi; služe samo za prikaz, backend svaku rutu ipak proverava sam
export function hasRole(role: string): boolean {
    return get(authStore).user?.roles.includes(role) ?? false;
}

// Forme sporta: ide posle requireAuth, pa je korisnik ovde već prijavljen
export function requireSystemAdmin(): boolean {
    if (!hasRole('SYSTEM_ADMIN')) {
        window.location.href = '#/sports';
        return false;
    }
    return true;
}
