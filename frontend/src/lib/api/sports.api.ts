import {apiFetch, errorMessage} from './client';
import type {SportRequest, SportResponse} from '../types/sport.types';

const BASE = '/api/v1/sports';

// includeInactive (i obrisani sportovi) backend dozvoljava samo uz permisiju sports.read_inactive
export async function getAllSports(includeInactive = false): Promise<SportResponse[]> {
    const res = await apiFetch(includeInactive ? `${BASE}?includeInactive=true` : BASE);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load sports'));
    return res.json();
}

export async function getSport(id: string): Promise<SportResponse> {
    const res = await apiFetch(`${BASE}/${id}`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Sport not found'));
    return res.json();
}

export async function createSport(data: SportRequest): Promise<SportResponse> {
    const res = await apiFetch(BASE, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to create sport'));
    return res.json();
}

export async function updateSport(id: string, data: SportRequest): Promise<SportResponse> {
    const res = await apiFetch(`${BASE}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to update sport'));
    return res.json();
}

// Brisanje je meko (active = false), pa se sport kasnije može vratiti sa restoreSport
export async function deleteSport(id: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to delete sport'));
}

export async function restoreSport(id: string): Promise<SportResponse> {
    const res = await apiFetch(`${BASE}/${id}/restore`, {method: 'PATCH'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to restore sport'));
    return res.json();
}
