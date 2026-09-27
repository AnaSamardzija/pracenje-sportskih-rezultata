import * as authApi from '../api/auth.api';
import {authStore} from './auth.store';
import {clearAuth, saveAuth} from '../stores/auth.storage';
import type {AuthState, RegisterRequest, StorageType} from '../types/auth.types';

export async function login(username: string, password: string, storageType: StorageType) {
    const {accessToken} = await authApi.login({username, password, storageType});
    startSession(accessToken, storageType);
}

export async function register(data: RegisterRequest) {
    const {accessToken} = await authApi.register(data);
    startSession(accessToken, data.storageType);
}

export function logout() {
    authStore.set({accessToken: null, storageType: null});
    clearAuth();
}

// Zajedničko za prijavu i registraciju: token i izabrana baza idu u store i u localStorage
function startSession(accessToken: string, storageType: StorageType) {
    const state: AuthState = {accessToken, storageType};

    authStore.set(state);
    saveAuth(state);
}
