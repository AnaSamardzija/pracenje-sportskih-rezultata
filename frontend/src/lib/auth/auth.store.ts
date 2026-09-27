import {writable} from 'svelte/store';
import type {AuthState} from '../types/auth.types';
import {loadAuth} from '../stores/auth.storage';

// Pri učitavanju aplikacije stanje se odmah puni iz localStorage-a, pa korisnik ostaje prijavljen posle F5
export const authStore = writable<AuthState>(loadAuth() ?? {accessToken: null, storageType: null, user: null});
