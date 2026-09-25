import {apiFetch, errorMessage} from './client';
import type {UserDto} from '../types/user.types';

const BASE = '/api/v1/users';

export async function getMe(): Promise<UserDto> {
    const res = await apiFetch(`${BASE}/me`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load user'));
    return res.json();
}
