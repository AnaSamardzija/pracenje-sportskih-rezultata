import {get} from 'svelte/store';
import * as authApi from '../api/auth.api';
import * as usersApi from '../api/users.api';
import {authStore} from './auth.store';
import {clearAuth, saveAuth} from '../stores/auth.storage';
import type {AuthState, RegisterRequest, StorageType} from '../types/auth.types';
import type {UserDto} from '../types/user.types';

export async function login(username: string, password: string, storageType: StorageType) {
    const {accessToken} = await authApi.login({username, password, storageType});
    await startSession(accessToken, storageType);
}

export async function register(data: RegisterRequest) {
    const {accessToken} = await authApi.register(data);
    await startSession(accessToken, data.storageType);
}

export function logout() {
    authStore.set({accessToken: null, storageType: null, user: null});
    clearAuth();
}

// Posle izmene profila korisnik u store-u se zameni odgovorom backend-a
export function setUser(user: UserDto) {
    const state: AuthState = {...get(authStore), user};

    authStore.set(state);
    saveAuth(state);
}

// Zajedničko za prijavu i registraciju: token i izabrana baza idu u store, pa se sa tim tokenom učita
// korisnik (odgovor na prijavu nosi samo token) i sve zajedno se upisuje u localStorage
async function startSession(accessToken: string, storageType: StorageType) {
    authStore.set({accessToken, storageType, user: null});

    try {
        const user = await usersApi.getMe();
        const state: AuthState = {accessToken, storageType, user};

        authStore.set(state);
        saveAuth(state);
    } catch (e) {
        logout();
        throw e;
    }
}
