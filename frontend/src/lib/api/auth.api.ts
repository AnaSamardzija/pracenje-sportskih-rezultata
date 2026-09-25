import {apiFetch, errorMessage} from './client';
import type {AuthRequest, AuthResponse} from '../types/auth.types';

const BASE = '/api/v1/auth';

export async function login(data: AuthRequest): Promise<AuthResponse> {
    const res = await apiFetch(`${BASE}/login`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Login failed'));
    return res.json();
}
