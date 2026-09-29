import {apiFetch, errorMessage} from './client';
import {withQuery} from '../utils/query';
import type {MatchRequest, MatchResponse} from '../types/match.types';

const BASE = '/api/v1/matches';

// Backend vraća mečeve od najnovijeg; nepostojeća grupa, sport ili igrač u filteru je 404
export async function getAllMatches(filters: {groupId?: string; sportId?: string; playerId?: string} = {}): Promise<MatchResponse[]> {
    const res = await apiFetch(withQuery(BASE, filters));
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load matches'));
    return res.json();
}

export async function getMatch(id: string): Promise<MatchResponse> {
    const res = await apiFetch(`${BASE}/${id}`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Match not found'));
    return res.json();
}

// Meč unosi bilo koji član grupe (uz permisiju matches.create_any i bez članstva); svi igrači moraju biti aktivni članovi iste grupe
export async function createMatch(data: MatchRequest): Promise<MatchResponse> {
    const res = await apiFetch(BASE, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to record match'));
    return res.json();
}

// Samo GROUP_ADMIN grupe ili permisija matches.update_any; grupa meča se ne može promeniti (422)
export async function updateMatch(id: string, data: MatchRequest): Promise<MatchResponse> {
    const res = await apiFetch(`${BASE}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to update match'));
    return res.json();
}

// Samo GROUP_ADMIN grupe ili permisija matches.delete_any (ostali dobijaju 403)
export async function deleteMatch(id: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to delete match'));
}
