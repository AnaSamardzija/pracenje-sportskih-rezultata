import {apiFetch, errorMessage} from './client';
import {withQuery} from '../utils/query';
import type {ChangePasswordRequest, UpdateProfileRequest, UpdateUserRequest, UserDto} from '../types/user.types';
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

// Statistiku bilo kog igrača vidi svaki prijavljen korisnik; sportId je ograničava na jedan sport
export async function getUserStats(id: string, sportId?: string): Promise<PlayerStatsResponse> {
    const res = await apiFetch(withQuery(`${BASE}/${id}/stats`, {sportId}));
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

// Upravljanje korisnicima (admin panel): svaka ruta ispod traži svoju permisiju (users.read_all, users.modify,
// users.roles.assign za promenu uloga, users.deactivate, users.restore).
// Spisak vraća i deaktivirane korisnike, ali ne i admina koji pita (on svoj nalog menja preko /me).
export async function getAllUsers(): Promise<UserDto[]> {
    const res = await apiFetch(BASE);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load users'));
    return res.json();
}

export async function getUser(id: string): Promise<UserDto> {
    const res = await apiFetch(`${BASE}/${id}`);
    if (!res.ok) throw new Error(await errorMessage(res, 'User not found'));
    return res.json();
}

export async function updateUser(id: string, data: UpdateUserRequest): Promise<UserDto> {
    const res = await apiFetch(`${BASE}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to update user'));
    return res.json();
}

// Nalog se ne briše, nego postaje neaktivan (active = false), pa se kasnije može vratiti sa restoreUser
export async function deactivateUser(id: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to deactivate user'));
}

export async function restoreUser(id: string): Promise<UserDto> {
    const res = await apiFetch(`${BASE}/${id}/restore`, {method: 'PATCH'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to restore user'));
    return res.json();
}
