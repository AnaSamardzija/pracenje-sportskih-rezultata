import {apiFetch, errorMessage} from './client';
import type {MatchRequest, MatchResponse} from '../types/match.types';

const BASE = '/api/v1/matches';

// Backend vraća mečeve od najnovijeg; nepostojeća grupa, sport ili igrač u filteru je 404
export async function getAllMatches(filters: {groupId?: string; sportId?: string; playerId?: string} = {}): Promise<MatchResponse[]> {
    const params = new URLSearchParams();
    if (filters.groupId) params.set('groupId', filters.groupId);
    if (filters.sportId) params.set('sportId', filters.sportId);
    if (filters.playerId) params.set('playerId', filters.playerId);

    const res = await apiFetch(params.size ? `${BASE}?${params}` : BASE);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load matches'));
    return res.json();
}

export async function getMatch(id: string): Promise<MatchResponse> {
    const res = await apiFetch(`${BASE}/${id}`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Match not found'));
    return res.json();
}

// Meč unosi bilo koji član grupe; svi igrači moraju biti aktivni članovi iste grupe
export async function createMatch(data: MatchRequest): Promise<MatchResponse> {
    const res = await apiFetch(BASE, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to record match'));
    return res.json();
}

// Samo učesnik meča ili GROUP_ADMIN; grupa meča se ne može promeniti (422)
export async function updateMatch(id: string, data: MatchRequest): Promise<MatchResponse> {
    const res = await apiFetch(`${BASE}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to update match'));
    return res.json();
}

// Samo učesnik meča ili GROUP_ADMIN grupe (ostali dobijaju 403)
export async function deleteMatch(id: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to delete match'));
}
