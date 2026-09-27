import {apiFetch, errorMessage} from './client';
import type {ChangePasswordRequest, UpdateProfileRequest, UserDto} from '../types/user.types';
import type {PlayerStatsResponse} from '../types/stats.types';

const BASE = '/api/v1/users';

export async function getMe(): Promise<UserDto> {
    const res = await apiFetch(`${BASE}/me`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load user'));
    return res.json();
}

export async function getMyStats(): Promise<PlayerStatsResponse> {
    const res = await apiFetch(`${BASE}/me/stats`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load statistics'));
    return res.json();
}

export async function updateMe(data: UpdateProfileRequest): Promise<UserDto> {
    const res = await apiFetch(`${BASE}/me`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to update profile'));
    return res.json();
}

// Backend vraća 204 bez tela; pogrešna stara lozinka je 422, pa korisnik ostaje prijavljen
export async function changePassword(data: ChangePasswordRequest): Promise<void> {
    const res = await apiFetch(`${BASE}/me/password`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to change password'));
}
