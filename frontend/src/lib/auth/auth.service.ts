import * as authApi from './auth.api';
import {authStore} from './auth.store';
import {clearAuth, saveAuth} from './auth.storage';
import type {AuthState, StorageType} from './auth.types';

export async function login(username: string, password: string, storageType: StorageType) {
    const {accessToken} = await authApi.login(username, password, storageType);
    const state: AuthState = {accessToken, storageType};

    authStore.set(state);
    saveAuth(state);
}

export function logout() {
    authStore.set({accessToken: null, storageType: null});
    clearAuth();
}
