import {apiFetch, errorMessage} from './client';
import type {GroupResponse} from '../types/group.types';

const BASE = '/api/v1/groups';

// mine = samo grupe prijavljenog korisnika, search = deo imena; oba filtera su opciona
export async function getAllGroups(filters: {mine?: boolean; search?: string} = {}): Promise<GroupResponse[]> {
    const params = new URLSearchParams();
    if (filters.mine) params.set('mine', 'true');
    if (filters.search) params.set('search', filters.search);

    const res = await apiFetch(params.size ? `${BASE}?${params}` : BASE);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load groups'));
    return res.json();
}
